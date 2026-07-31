package com.spaceflow.room;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Room CRUD 리포지토리.
 * JpaRepository를 상속하면 findById/save/findAll 등 기본 메서드가 자동 제공된다.
 */
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * 방 행을 비관적 쓰기 잠금(SELECT ... FOR UPDATE)으로 조회한다.
     * 같은 방을 예약하려는 다른 트랜잭션은 이 잠금이 풀릴 때까지 대기 → 예약 처리가 직렬화된다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    /**
     * 방을 낙관적 락으로 조회하되, 커밋 시 version을 강제로 +1 한다(OPTIMISTIC_FORCE_INCREMENT).
     * 같은 방을 동시에 예약하면 커밋 시점에 version 충돌이 나 진 쪽이 롤백된다.
     */
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForOptimisticLock(@Param("id") Long id);
}
