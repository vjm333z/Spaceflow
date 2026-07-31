package com.spaceflow.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * 모든 엔티티가 공통으로 갖는 생성시각(created_at)을 모아둔 상위 클래스.
 * 테이블이 되진 않고(@MappedSuperclass), 상속한 엔티티의 컬럼으로 합쳐진다.
 */
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // 저장 직전에 생성시각을 자동으로 채운다
    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
