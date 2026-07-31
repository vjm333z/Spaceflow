package com.spaceflow.room;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Room CRUD 리포지토리.
 * JpaRepository를 상속하면 findById/save/findAll 등 기본 메서드가 자동 제공된다.
 */
public interface RoomRepository extends JpaRepository<Room, Long> {
}
