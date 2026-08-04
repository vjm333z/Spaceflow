package com.spaceflow.pricing;

import com.spaceflow.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * 요금 규칙 한 건. 조건(matches)에 걸리면 조정액(adjustmentOn)을 계산해 적용한다.
 * 규칙을 데이터로 관리하므로, 새 규칙 추가 시 코드 수정 없이 이 테이블에 행만 넣으면 된다.
 */
@Entity
@Table(name = "pricing_rule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PricingRule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 여기선 Room 객체가 필요 없어 FK id만 단순 보관 (연관관계 매핑 생략)
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, length = 20)
    private RuleType ruleType;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;   // 1=월 ... 7=일

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "min_hours")
    private Integer minHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjust_type", nullable = false, length = 10)
    private AdjustType adjustType;

    @Column(name = "adjust_value", nullable = false)
    private BigDecimal adjustValue;

    @Column(nullable = false)
    private int priority;

    /**
     * 이 규칙이 해당 예약에 적용되는지. (시간·요일 판정은 영업 지역시간으로 변환된 값을 받는다)
     */
    public boolean matches(ZonedDateTime startLocal, ZonedDateTime endLocal, BigDecimal hours) {
        return switch (ruleType) {
            // 예약 시간대가 규칙 시간대 [start, end) 와 겹치면 적용
            case TIME_OF_DAY -> startLocal.toLocalTime().isBefore(endTime)
                    && startTime.isBefore(endLocal.toLocalTime());
            case DAY_OF_WEEK -> startLocal.getDayOfWeek().getValue() == dayOfWeek;
            case DURATION -> hours.compareTo(new BigDecimal(minHours)) >= 0;
        };
    }

    /**
     * 현재 running 금액에 대한 조정액(델타)을 계산한다. (음수면 할인)
     */
    public BigDecimal adjustmentOn(BigDecimal running) {
        return switch (adjustType) {
            case PERCENT -> running.multiply(adjustValue)
                    .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            case FIXED -> adjustValue;
        };
    }

    /** 견적 내역에 표시할 사람이 읽을 라벨 */
    public String label() {
        String cond = switch (ruleType) {
            case TIME_OF_DAY -> "피크시간(" + startTime + "~" + endTime + ")";
            case DAY_OF_WEEK -> dayName(dayOfWeek);
            case DURATION -> minHours + "시간 이상";
        };
        String adj = adjustType == AdjustType.PERCENT
                ? sign(adjustValue) + adjustValue.stripTrailingZeros().toPlainString() + "%"
                : sign(adjustValue) + adjustValue.stripTrailingZeros().toPlainString() + "원";
        return cond + " " + adj;
    }

    private static String sign(BigDecimal v) {
        return v.signum() >= 0 ? "+" : "";
    }

    private static String dayName(Integer dow) {
        String[] names = {"", "월", "화", "수", "목", "금", "토", "일"};
        return (dow != null && dow >= 1 && dow <= 7 ? names[dow] : "?") + "요일";
    }
}
