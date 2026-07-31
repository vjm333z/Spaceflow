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
     * 이 확인과 저장 사이에 다른 요청이 끼어들 수 있다(경쟁상태). 동시성 단계에서 이걸 터뜨린다.
     */
    @Transactional
    public ReservationResponse reserve(CreateReservationRequest req) {
        Room room = roomRepository.findById(req.roomId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + req.roomId()));

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
