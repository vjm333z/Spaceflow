package com.spaceflow.reservation;

import com.spaceflow.pricing.PricingService;
import com.spaceflow.room.Room;
import com.spaceflow.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PricingService pricingService;

    /**
     * 예약 생성 — ✅ 운영 채택 방식: 앱 사전확인(친절한 에러) + DB EXCLUDE 제약(정합성 보장).
     * 대부분의 겹침은 existsOverlapping에서 걸러 409로 안내하고,
     * 그 확인을 뚫고 들어온 동시 요청은 reservation_no_overlap 제약이 최후 방어선으로 막는다.
     */
    @Transactional
    public ReservationResponse reserve(CreateReservationRequest req) {
        Room room = roomRepository.findById(req.roomId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + req.roomId()));
        return checkAndSave(room, req);
    }

    /**
     * 비관적 락 버전 (동시성 방식 비교용).
     * 방(Room) 행을 SELECT ... FOR UPDATE 로 잠가 같은 방 예약을 직렬화한다.
     */
    @Transactional
    public ReservationResponse reserveWithPessimisticLock(CreateReservationRequest req) {
        Room room = roomRepository.findByIdForUpdate(req.roomId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + req.roomId()));
        return checkAndSave(room, req);
    }

    /**
     * 낙관적 락 버전 (동시성 방식 비교용).
     * 방의 version을 강제 증가시켜 커밋 시점에 충돌을 감지한다.
     */
    @Transactional
    public ReservationResponse reserveWithOptimisticLock(CreateReservationRequest req) {
        Room room = roomRepository.findByIdForOptimisticLock(req.roomId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + req.roomId()));
        return checkAndSave(room, req);
    }

    private ReservationResponse checkAndSave(Room room, CreateReservationRequest req) {
        // 1) 앱 사전확인 — 대부분의 겹침을 친절한 메시지로 거른다 (UX)
        if (reservationRepository.existsOverlapping(req.roomId(), req.startAt(), req.endAt())) {
            throw new IllegalStateException("이미 예약된 시간대입니다.");
        }
        // 예약 확정 시점의 요금을 계산해 스냅샷으로 저장한다
        BigDecimal price = pricingService.quote(req.roomId(), req.startAt(), req.endAt()).total();
        try {
            // 2) 저장 — 사전확인을 뚫은 찰나의 동시 요청은 DB EXCLUDE 제약이 막는다 (정합성)
            Reservation saved = reservationRepository.save(
                    new Reservation(room, req.startAt(), req.endAt(), req.guestName(), req.guestPhone(), price));
            return ReservationResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            // reservation_no_overlap 제약 위반 = 그 찰나에 겹치는 예약이 먼저 커밋됐다
            throw new IllegalStateException("이미 예약된 시간대입니다.");
        }
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
}
