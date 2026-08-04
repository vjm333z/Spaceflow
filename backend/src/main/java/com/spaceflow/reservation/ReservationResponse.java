package com.spaceflow.reservation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 예약 응답 DTO. 엔티티를 그대로 노출하지 않고 필요한 값만 담아 내보낸다.
 */
public record ReservationResponse(
        Long id,
        Long roomId,
        OffsetDateTime startAt,
        OffsetDateTime endAt,
        String status,
        String guestName,
        BigDecimal price
) {
    static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getRoom().getId(),
                r.getStartAt(),
                r.getEndAt(),
                r.getStatus().name(),
                r.getGuestName(),
                r.getPrice()
        );
    }
}
