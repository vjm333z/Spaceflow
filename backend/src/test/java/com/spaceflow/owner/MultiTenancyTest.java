package com.spaceflow.owner;

import com.spaceflow.TestcontainersConfiguration;
import com.spaceflow.auth.AuthService;
import com.spaceflow.auth.LoginRequest;
import com.spaceflow.reservation.Reservation;
import com.spaceflow.reservation.ReservationRepository;
import com.spaceflow.room.Room;
import com.spaceflow.room.RoomRepository;
import com.spaceflow.space.Space;
import com.spaceflow.space.SpaceRepository;
import com.spaceflow.tenant.Tenant;
import com.spaceflow.tenant.TenantRepository;
import com.spaceflow.user.Role;
import com.spaceflow.user.User;
import com.spaceflow.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 멀티테넌시 격리 검증: 사장은 자기 테넌트 예약만 보고, 다른 테넌트 데이터는 못 본다.
 * tenantId를 JWT에서 꺼내므로 사용자가 남의 테넌트를 지정할 수 없다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class MultiTenancyTest {

    @Autowired MockMvc mvc;
    @Autowired TenantRepository tenantRepository;
    @Autowired SpaceRepository spaceRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired AuthService authService;

    @BeforeEach
    void clean() {
        reservationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void 사장은_자기_테넌트_예약만_본다() throws Exception {
        // tenant1: 시드된 room1(id=1). tenant2: 새로 생성.
        Room room1 = roomRepository.findById(1L).orElseThrow();
        Tenant tenant2 = tenantRepository.save(new Tenant("둘째 스터디카페"));
        Space space2 = spaceRepository.save(new Space(tenant2, "판교점"));
        Room room2 = roomRepository.save(new Room(space2, "회의실 B", 6, new BigDecimal("12000")));

        // 각 테넌트에 예약 1건씩
        reservationRepository.save(new Reservation(room1,
                odt("2026-10-01T10:00:00+09:00"), odt("2026-10-01T11:00:00+09:00"),
                "테넌트1손님", null, new BigDecimal("10000")));
        reservationRepository.save(new Reservation(room2,
                odt("2026-10-01T10:00:00+09:00"), odt("2026-10-01T11:00:00+09:00"),
                "테넌트2손님", null, new BigDecimal("12000")));

        // 각 테넌트 사장 계정
        userRepository.save(new User("owner1@demo.com", passwordEncoder.encode("password123"), Role.OWNER, 1L));
        userRepository.save(new User("owner2@demo.com", passwordEncoder.encode("password123"), Role.OWNER, tenant2.getId()));

        String token1 = authService.login(new LoginRequest("owner1@demo.com", "password123")).accessToken();
        String token2 = authService.login(new LoginRequest("owner2@demo.com", "password123")).accessToken();

        // 사장1 → tenant1 예약만
        mvc.perform(get("/api/owner/reservations").header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].guestName").value("테넌트1손님"));

        // 사장2 → tenant2 예약만
        mvc.perform(get("/api/owner/reservations").header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].guestName").value("테넌트2손님"));
    }

    @Test
    void 토큰_없으면_401() throws Exception {
        mvc.perform(get("/api/owner/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 손님_토큰이면_403() throws Exception {
        userRepository.save(new User("guest@demo.com", passwordEncoder.encode("password123"), Role.GUEST, null));
        String guestToken = authService.login(new LoginRequest("guest@demo.com", "password123")).accessToken();
        mvc.perform(get("/api/owner/reservations").header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isForbidden());
    }

    private static OffsetDateTime odt(String s) {
        return OffsetDateTime.parse(s);
    }
}
