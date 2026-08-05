package com.spaceflow.pricing;

import com.spaceflow.room.Room;
import com.spaceflow.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 요금 계산 엔진.
 * 기본요금(시간당 × 이용시간)에 방의 요금 규칙들을 priority 순서로 적용해 최종 금액과 내역을 만든다.
 */
@Service
@RequiredArgsConstructor
public class PricingService {

    // 피크시간·요일 판정 기준이 되는 영업 지역시간 (지금은 KST 고정, 추후 테넌트별로 확장 가능)
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Seoul");

    private final RoomRepository roomRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public PriceQuote quote(Long roomId, OffsetDateTime startAt, OffsetDateTime endAt) {
        return quote(roomId, startAt, endAt, null);
    }

    @Transactional(readOnly = true)
    public PriceQuote quote(Long roomId, OffsetDateTime startAt, OffsetDateTime endAt, String couponCode) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다: " + roomId));
        if (!endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("종료 시각이 시작 시각보다 뒤여야 합니다.");
        }

        // 이용 시간(시간 단위, 소수 2자리) — 예: 90분 → 1.50
        BigDecimal hours = BigDecimal.valueOf(Duration.between(startAt, endAt).toMinutes())
                .divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        BigDecimal baseAmount = room.getBasePricePerHour().multiply(hours).setScale(2, RoundingMode.HALF_UP);

        // 시간대·요일 판정은 영업 지역시간 기준
        ZonedDateTime startLocal = startAt.atZoneSameInstant(BUSINESS_ZONE);
        ZonedDateTime endLocal = endAt.atZoneSameInstant(BUSINESS_ZONE);

        List<PriceQuote.PriceLine> lines = new ArrayList<>();
        BigDecimal running = baseAmount;
        for (PricingRule rule : pricingRuleRepository.findByRoomIdOrderByPriorityAsc(roomId)) {
            if (rule.matches(startLocal, endLocal, hours)) {
                BigDecimal delta = rule.adjustmentOn(running);
                running = running.add(delta);
                lines.add(new PriceQuote.PriceLine(rule.label(), delta));
            }
        }

        // 쿠폰 할인 — 규칙 적용 후 최종 금액에 적용
        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponRepository.findByCodeIgnoreCaseAndActiveTrue(couponCode.trim()).orElse(null);
            if (coupon != null) {
                BigDecimal discount = coupon.discountOn(running).negate();
                running = running.add(discount);
                lines.add(new PriceQuote.PriceLine(coupon.label(), discount.setScale(2, RoundingMode.HALF_UP)));
            }
        }

        // 최종 금액은 원 단위로 반올림
        BigDecimal total = running.setScale(0, RoundingMode.HALF_UP);
        return new PriceQuote(roomId, hours, room.getBasePricePerHour(), baseAmount, lines, total);
    }
}
