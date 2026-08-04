package com.spaceflow.owner;

import com.spaceflow.reservation.ReservationResponse;
import com.spaceflow.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 사업자(OWNER) 전용 API. 멀티테넌시 격리의 핵심:
 * tenantId를 요청 파라미터가 아니라 인증된 사장의 JWT에서 꺼내 쓴다 → 남의 테넌트 조회 불가.
 */
@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final ReservationService reservationService;

    @GetMapping("/reservations")
    @PreAuthorize("hasRole('OWNER')")
    public List<ReservationResponse> myReservations(@AuthenticationPrincipal Jwt jwt) {
        return reservationService.reservationsForTenant(tenantIdOf(jwt));
    }

    private static Long tenantIdOf(Jwt jwt) {
        Object claim = jwt.getClaim("tenantId");
        if (claim == null) {
            throw new IllegalStateException("테넌트에 소속되지 않은 계정입니다.");
        }
        return ((Number) claim).longValue();
    }
}
