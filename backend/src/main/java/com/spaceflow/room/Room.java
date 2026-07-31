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

    // 낙관적 락용 버전 (예약 시 강제 증가시켜 동시 예약 충돌을 감지하는 지점으로 쓴다)
    @Version
    private Long version;

    public Room(Space space, String name, int capacity) {
        this.space = space;
        this.name = name;
        this.capacity = capacity;
    }
}
