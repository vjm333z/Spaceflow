package com.spaceflow.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

/**
 * 예약 생성 요청 본문. record라 필드·생성자·getter가 자동으로 만들어진다.
 * @NotNull 등은 컨트롤러에서 @Valid로 검증된다.
 */
public record CreateReservationRequest(
        @NotNull Long roomId,
        @NotNull OffsetDateTime startAt,
        @NotNull OffsetDateTime endAt,
        @NotBlank String guestName,
        String guestPhone,
        String couponCode
) {
}
