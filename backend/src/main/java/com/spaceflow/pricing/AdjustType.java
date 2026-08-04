package com.spaceflow.pricing;

/**
 * 요금 조정 방식.
 * - PERCENT : 현재 금액에 퍼센트로 가감 (예: +50 → ×1.5, -10 → ×0.9)
 * - FIXED   : 정액으로 가감 (원)
 */
public enum AdjustType {
    PERCENT,
    FIXED
}
