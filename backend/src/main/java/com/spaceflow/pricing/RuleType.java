package com.spaceflow.pricing;

/**
 * 요금 규칙의 종류.
 * - TIME_OF_DAY : 특정 시간대(피크 등)에 예약이 걸치면 적용
 * - DAY_OF_WEEK : 특정 요일이면 적용
 * - DURATION    : 이용 시간이 기준 이상이면 적용(장시간 할인 등)
 */
public enum RuleType {
    TIME_OF_DAY,
    DAY_OF_WEEK,
    DURATION
}
