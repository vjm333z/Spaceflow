package com.spaceflow.reservation;

import com.spaceflow.room.Room;
import com.spaceflow.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    /**
     * 예약 생성 — ⚠️ 순진한(naive) 버전.
     * "겹치는 예약이 있나?" 확인(check) 후 "없으면 저장(act)" 하는데,
     * 확인과 저장 사이에 다른 요청이 끼어들 수 있다(경쟁상태). → 동시 요청 시 중복 예약 발생.
     */
    @Transactional
    public ReservationResponse reserve(CreateReservationRequest req) {
        Room room = roomRepository.findById(req.roomId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + req.roomId()));
        return checkAndSave(room, req);
    }

    /**
     * 예약 생성 — 비관적 락 버전.
     * 방(Room) 행을 SELECT ... FOR UPDATE 로 잠근 뒤 확인·저장하므로,
     * 같은 방에 대한 예약이 한 번에 하나씩 직렬 처리된다 → 중복 예약이 막힌다.
     */
    @Transactional
    public ReservationResponse reserveWithPessimisticLock(CreateReservationRequest req) {
        Room room = roomRepository.findByIdForUpdate(req.roomId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + req.roomId()));
        return checkAndSave(room, req);
    }

    // 겹침 확인 후 저장하는 공통 로직 (방어 방식만 위에서 다르게 감싼다)
    private ReservationResponse checkAndSave(Room room, CreateReservationRequest req) {
        boolean overlap = reservationRepository.existsOverlapping(req.roomId(), req.startAt(), req.endAt());
        if (overlap) {
            throw new IllegalStateException("이미 예약된 시간대입니다.");
        }
        Reservation saved = reservationRepository.save(
                new Reservation(room, req.startAt(), req.endAt(), req.guestName(), req.guestPhone()));
        return ReservationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findByRoom(Long roomId) {
        return reservationRepository.findByRoomIdOrderByStartAtAsc(roomId).stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
