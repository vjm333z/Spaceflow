package com.spaceflow.room;

import com.spaceflow.common.BaseTimeEntity;
import com.spaceflow.space.Space;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 방 — 예약 대상 단위. 특정 지점에 속한다. (예: "4인실 A", 정원 4)
 */
@Entity
@Table(name = "room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "space_id")
    private Space space;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int capacity;

    // 시간당 기본요금 (원). 돈은 오차 방지를 위해 BigDecimal 사용.
    @Column(name = "base_price_per_hour", nullable = false)
    private BigDecimal basePricePerHour;

    // 낙관적 락용 버전 (예약 시 강제 증가시켜 동시 예약 충돌을 감지하는 지점으로 쓴다)
    @Version
    private Long version;

    public Room(Space space, String name, int capacity, BigDecimal basePricePerHour) {
        this.space = space;
        this.name = name;
        this.capacity = capacity;
        this.basePricePerHour = basePricePerHour;
    }

    // 사장이 방 정보를 수정
    public void update(String name, int capacity, BigDecimal basePricePerHour) {
        this.name = name;
        this.capacity = capacity;
        this.basePricePerHour = basePricePerHour;
    }

    // 이 방이 특정 테넌트 소유인지 (멀티테넌시 권한 검사)
    public boolean belongsToTenant(Long tenantId) {
        return getSpace().getTenant().getId().equals(tenantId);
    }
}
