package com.spaceflow.owner;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** 방 생성/수정 요청. */
public record RoomForm(
        @NotBlank String name,
        @Min(1) int capacity,
        @NotNull @DecimalMin("0") BigDecimal basePricePerHour
) {
}
