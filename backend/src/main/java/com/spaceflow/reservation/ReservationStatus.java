package com.spaceflow.reservation;

/**
 * 예약 상태.
 * - PENDING   : 대기(결제 전 등)
 * - CONFIRMED : 확정 — 방을 실제로 점유한다
 * - CANCELLED : 취소 — 방을 더 이상 막지 않는다
 */
public enum ReservationStatus {
    PENDING,
    CONFIRMED,
    CANCELLED
}
