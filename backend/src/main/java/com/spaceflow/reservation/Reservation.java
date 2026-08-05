package com.spaceflow.reservation;

import com.spaceflow.common.BaseTimeEntity;
import com.spaceflow.room.Room;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.OffsetDateTime;

/**
 * 예약 — 특정 방을 특정 시간구간(startAt ~ endAt)에 잡는다.
 * 구간은 반열린 [startAt, endAt): 끝시각은 포함하지 않으므로 10~11시와 11~12시는 겹치지 않는다.
 */
@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "guest_name", nullable = false, length = 50)
    private String guestName;

    @Column(name = "guest_phone", length = 30)
    private String guestPhone;

    // 예약 확정 시점의 요금 스냅샷 (정책이 바뀌어도 과거 금액 보존)
    @Column
    private BigDecimal price;

    // 예약한 로그인 사용자 (비로그인 예약이면 null)
    @Column(name = "user_id")
    private Long userId;

    // 낙관적 락: 동시 수정 충돌을 감지하는 버전 컬럼 (동시성 단계에서 활용)
    @Version
    private Long version;

    public Reservation(Room room, OffsetDateTime startAt, OffsetDateTime endAt,
                       String guestName, String guestPhone, BigDecimal price, Long userId) {
        this.room = room;
        this.startAt = startAt;
        this.endAt = endAt;
        this.guestName = guestName;
        this.guestPhone = guestPhone;
        this.price = price;
        this.userId = userId;
        this.status = ReservationStatus.PENDING; // 결제 전 대기
    }

    // 결제 완료 → 확정 (대기 상태일 때만)
    public void confirm() {
        if (this.status == ReservationStatus.PENDING) {
            this.status = ReservationStatus.CONFIRMED;
        }
    }

    // 취소는 상태 변경으로만 (레코드를 지우지 않는다 — 이력 보존)
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}
