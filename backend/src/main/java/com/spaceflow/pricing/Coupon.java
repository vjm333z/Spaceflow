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

/** 할인 쿠폰. 요금 규칙 적용 후 최종 금액에 할인을 적용한다. */
@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 10)
    private AdjustType discountType;

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private boolean active;

    /** 주어진 금액에 대한 할인액(양수). FIXED는 금액을 넘지 않도록 제한. */
    public BigDecimal discountOn(BigDecimal amount) {
        return switch (discountType) {
            case PERCENT -> amount.multiply(discountValue).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
            case FIXED -> discountValue.min(amount);
        };
    }

    public String label() {
        String v = discountValue.stripTrailingZeros().toPlainString();
        return "쿠폰 " + code + (discountType == AdjustType.PERCENT ? " -" + v + "%" : " -" + v + "원");
    }
}
