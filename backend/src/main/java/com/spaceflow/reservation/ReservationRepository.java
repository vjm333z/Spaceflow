package com.spaceflow.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * 특정 방에서 주어진 시간구간 [startAt, endAt) 과 겹치는 예약이 있는지.
     * 겹침 조건: 기존.startAt < 새.endAt  AND  새.startAt < 기존.endAt
     * 취소(CANCELLED)된 예약은 방을 막지 않으므로 제외한다.
     */
    @Query("""
            select count(r) > 0
            from Reservation r
            where r.room.id = :roomId
              and r.status <> com.spaceflow.reservation.ReservationStatus.CANCELLED
              and r.startAt < :endAt
              and :startAt < r.endAt
            """)
    boolean existsOverlapping(@Param("roomId") Long roomId,
                              @Param("startAt") OffsetDateTime startAt,
                              @Param("endAt") OffsetDateTime endAt);

    List<Reservation> findByRoomIdOrderByStartAtAsc(Long roomId);
}
