package com.spaceflow.reservation;

import java.time.OffsetDateTime;

/** 예약된 시간대(가용시간 표시용). 개인정보 없이 시간 범위만 노출. */
public record BookedSlot(OffsetDateTime startAt, OffsetDateTime endAt) {
    static BookedSlot from(Reservation r) {
        return new BookedSlot(r.getStartAt(), r.getEndAt());
    }
}
