package com.spaceflow.room;

import java.math.BigDecimal;

/** 방 목록/조회 응답 DTO. */
public record RoomResponse(
        Long id,
        String name,
        int capacity,
        BigDecimal basePricePerHour
) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCapacity(), room.getBasePricePerHour());
    }
}
