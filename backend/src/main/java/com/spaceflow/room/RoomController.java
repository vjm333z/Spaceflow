package com.spaceflow.room;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 방 조회 API (공개). 손님이 예약할 방 목록을 본다. */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;

    @GetMapping
    public List<RoomResponse> list() {
        return roomRepository.findAll().stream()
                .map(RoomResponse::from)
                .toList();
    }
}
