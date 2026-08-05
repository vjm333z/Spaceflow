package com.spaceflow.reservation;

import com.spaceflow.pricing.PricingService;
import com.spaceflow.room.Room;
import com.spaceflow.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalTime OPEN = LocalTime.of(8, 0);
    private static final LocalTime CLOSE = LocalTime.of(22, 0);
    private static final long MAX_HOURS = 8;

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PricingService pricingService;

    /**
     * 예약 생성 — ✅ 운영 채택 방식: 비관적 락(방 행 FOR UPDATE) + DB EXCLUDE 제약(최후 방어선).
     *
     * 처음엔 "EXCLUDE 제약 + 앱 사전확인"만 썼으나, k6 부하테스트(동시 100요청)에서
     * 여러 트랜잭션이 GiST 인덱스 락을 제각각 순서로 잡아 데드락 + 커넥션풀 고갈(30초 타임아웃)이 발생했다.
     * → 방(Room) 행을 먼저 잠가 같은 방 예약을 단일 순서로 직렬화하면 순환 대기가 사라져 데드락이 없어진다.
     *   각 처리가 빨라 커넥션도 즉시 반납되어 타임아웃도 해소. EXCLUDE 제약은 정합성 백스톱으로 유지.
     */
    @Transactional
    public ReservationResponse reserve(CreateReservationRequest req) {
        return reserve(req, null); // 비로그인 예약
    }

    @Transactional
    public ReservationResponse reserve(CreateReservationRequest req, Long userId) {
        Room room = roomRepository.findByIdForUpdate(req.roomId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + req.roomId()));
        return checkAndSave(room, req, userId);
    }

    /**
     * 비관적 락 버전 (동시성 방식 비교용).
     * 방(Room) 행을 SELECT ... FOR UPDATE 로 잠가 같은 방 예약을 직렬화한다.
     */
    @Transactional
    public ReservationResponse reserveWithPessimisticLock(CreateReservationRequest req) {
        Room room = roomRepository.findByIdForUpdate(req.roomId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + req.roomId()));
        return checkAndSave(room, req, null);
    }

    /**
     * 낙관적 락 버전 (동시성 방식 비교용).
     * 방의 version을 강제 증가시켜 커밋 시점에 충돌을 감지한다.
     */
    @Transactional
    public ReservationResponse reserveWithOptimisticLock(CreateReservationRequest req) {
        Room room = roomRepository.findByIdForOptimisticLock(req.roomId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + req.roomId()));
        return checkAndSave(room, req, null);
    }

    private ReservationResponse checkAndSave(Room room, CreateReservationRequest req, Long userId) {
        validateBookingTime(req);
        // 1) 앱 사전확인 — 대부분의 겹침을 친절한 메시지로 거른다 (UX)
        if (reservationRepository.existsOverlapping(req.roomId(), req.startAt(), req.endAt())) {
            throw new IllegalStateException("이미 예약된 시간대입니다.");
        }
        // 예약 확정 시점의 요금을 계산해 스냅샷으로 저장한다 (쿠폰 반영)
        BigDecimal price = pricingService.quote(req.roomId(), req.startAt(), req.endAt(), req.couponCode()).total();
        try {
            // 2) 저장 — 사전확인을 뚫은 찰나의 동시 요청은 DB EXCLUDE 제약이 막는다 (정합성)
            Reservation saved = reservationRepository.save(
                    new Reservation(room, req.startAt(), req.endAt(), req.guestName(), req.guestPhone(), price, userId));
            return ReservationResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            // reservation_no_overlap 제약 위반 = 그 찰나에 겹치는 예약이 먼저 커밋됐다
            throw new IllegalStateException("이미 예약된 시간대입니다.");
        }
    }

    // 예약 규칙 검증: 지난 시간 금지, 영업시간(08~22) 내, 최대 8시간
    private void validateBookingTime(CreateReservationRequest req) {
        if (!req.endAt().isAfter(req.startAt())) {
            throw new IllegalArgumentException("종료 시각이 시작 시각보다 뒤여야 합니다.");
        }
        if (req.startAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("지난 시간은 예약할 수 없습니다.");
        }
        if (Duration.between(req.startAt(), req.endAt()).toMinutes() > MAX_HOURS * 60) {
            throw new IllegalArgumentException("최대 " + MAX_HOURS + "시간까지 예약할 수 있습니다.");
        }
        ZonedDateTime start = req.startAt().atZoneSameInstant(BUSINESS_ZONE);
        ZonedDateTime end = req.endAt().atZoneSameInstant(BUSINESS_ZONE);
        boolean outOfHours = start.toLocalTime().isBefore(OPEN)
                || end.toLocalTime().isAfter(CLOSE)
                || end.toLocalDate().isAfter(start.toLocalDate()); // 자정 넘김 금지
        if (outOfHours) {
            throw new IllegalArgumentException("영업시간(08:00~22:00) 내에서 예약할 수 있습니다.");
        }
    }

    /** 특정 방·날짜의 예약된 시간대 목록 (가용시간 슬롯 표시용). */
    @Transactional(readOnly = true)
    public List<BookedSlot> bookedSlots(Long roomId, LocalDate date) {
        OffsetDateTime dayStart = date.atStartOfDay(BUSINESS_ZONE).toOffsetDateTime();
        OffsetDateTime dayEnd = date.plusDays(1).atStartOfDay(BUSINESS_ZONE).toOffsetDateTime();
        return reservationRepository.findActiveByRoomAndRange(roomId, dayStart, dayEnd).stream()
                .map(BookedSlot::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findByRoom(Long roomId) {
        return reservationRepository.findByRoomIdOrderByStartAtAsc(roomId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    /** 특정 테넌트(사업자)의 예약 목록. tenantId는 인증된 사장의 JWT에서 온다. */
    @Transactional(readOnly = true)
    public List<ReservationResponse> reservationsForTenant(Long tenantId) {
        return reservationRepository.findByTenantId(tenantId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    /** 내 예약 목록 (로그인 사용자). */
    @Transactional(readOnly = true)
    public List<ReservationResponse> myReservations(Long userId) {
        return reservationRepository.findByUserIdOrderByStartAtDesc(userId).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    /** 예약 취소 — 본인 예약만 가능. 레코드를 지우지 않고 상태만 CANCELLED로. */
    @Transactional
    public void cancel(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        if (!userId.equals(reservation.getUserId())) {
            throw new AccessDeniedException("본인 예약만 취소할 수 있습니다.");
        }
        reservation.cancel();
    }

    /** 결제(모의) → 예약 확정. PENDING을 CONFIRMED로. */
    @Transactional
    public ReservationResponse pay(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("취소된 예약은 결제할 수 없습니다.");
        }
        reservation.confirm();
        return ReservationResponse.from(reservation);
    }
}
