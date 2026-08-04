package com.spaceflow.pricing;

import java.math.BigDecimal;
import java.util.List;

/**
 * 요금 견적 결과 (내역 포함).
 * baseAmount(기본요금 × 시간)에서 규칙들이 순서대로 적용된 뒤 total이 나온다.
 */
public record PriceQuote(
        Long roomId,
        BigDecimal hours,
        BigDecimal basePricePerHour,
        BigDecimal baseAmount,
        List<PriceLine> lines,   // 적용된 규칙별 조정 내역
        BigDecimal total
) {
    /** 견적 내역 한 줄 (규칙 라벨 + 조정액) */
    public record PriceLine(String label, BigDecimal amount) {
    }
}
