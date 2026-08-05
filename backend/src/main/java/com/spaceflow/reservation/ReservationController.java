package com.spaceflow.reservation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // 공개: 비로그인도 예약 가능. 로그인 상태면 JWT의 userId를 예약에 연결한다.
    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid CreateReservationRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = (jwt != null) ? Long.valueOf(jwt.getSubject()) : null;
        ReservationResponse res = reservationService.reserve(req, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    public List<ReservationResponse> list(@RequestParam Long roomId) {
        return reservationService.findByRoom(roomId);
    }

    // 인증 필요: 내 예약 목록
    @GetMapping("/mine")
    public List<ReservationResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return reservationService.myReservations(Long.valueOf(jwt.getSubject()));
    }

    // 공개: 모의 결제 → 예약 확정 (방금 예약한 사람이 결제)
    @PostMapping("/{id}/pay")
    public ReservationResponse pay(@PathVariable Long id) {
        return reservationService.pay(id);
    }

    // 인증 필요: 내 예약 취소
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        reservationService.cancel(id, Long.valueOf(jwt.getSubject()));
        return ResponseEntity.noContent().build();
    }
}
