package com.spaceflow.room;

import com.spaceflow.reservation.BookedSlot;
import com.spaceflow.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** 방 조회 API (공개). 손님이 예약할 방 목록과 가용시간을 본다. */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;
    private final ReservationService reservationService;

    @GetMapping
    public List<RoomResponse> list() {
        return roomRepository.findAll().stream()
                .map(RoomResponse::from)
                .toList();
    }

    /** 특정 방·날짜의 예약된 시간대 (가용시간 슬롯 표시용) */
    @GetMapping("/{roomId}/availability")
    public List<BookedSlot> availability(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return reservationService.bookedSlots(roomId, date);
    }
}
