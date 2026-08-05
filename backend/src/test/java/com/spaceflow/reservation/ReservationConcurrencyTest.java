package com.spaceflow.reservation;

import com.spaceflow.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 동시성 방어 검증.
 * 같은 방·같은 시간을 동시에 예약하면 어떤 방식이든 CONFIRMED가 딱 1건이어야 한다.
 * (순진한 버전이 중복 10건을 만들던 "before"는 git 히스토리 커밋 ddc90f9에 남아있다.
 *  이제 EXCLUDE 제약이 DB 차원에서 막으므로 운영 방식조차 중복이 생기지 않는다.)
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReservationConcurrencyTest {

    private static final Long ROOM_ID = 1L;

    @Autowired
    ReservationService reservationService;
    @Autowired
    ReservationRepository reservationRepository;

    @BeforeEach
    void clean() {
        reservationRepository.deleteAll();
    }

    // --- 같은 시간에 20개 동시 예약 → 어떤 방식이든 1건만 성공해야 한다 ---

    @Test
    void 운영방식_앱확인_더하기_EXCLUDE제약이면_1건만_성공한다() throws InterruptedException {
        assertThat(fireSameSlot(reservationService::reserve)).isEqualTo(1);
    }

    @Test
    void 비관적_락도_1건만_성공한다() throws InterruptedException {
        assertThat(fireSameSlot(reservationService::reserveWithPessimisticLock)).isEqualTo(1);
    }

    @Test
    void 낙관적_락도_1건만_성공한다() throws InterruptedException {
        assertThat(fireSameSlot(reservationService::reserveWithOptimisticLock)).isEqualTo(1);
    }

    // --- EXCLUDE의 강점: 겹치지 않는 예약은 동시에 모두 성공한다 (락과 달리 직렬화 안 됨) ---

    @Test
    void 겹치지_않는_예약은_동시에_모두_성공한다() throws InterruptedException {
        int n = 10;
        AtomicInteger success = fireDifferentSlots(n);
        long confirmed = countConfirmed();
        System.out.printf("[동시성-비겹침] 성공저장=%d, DB의 CONFIRMED=%d%n", success.get(), confirmed);
        assertThat(confirmed).isEqualTo(n);
    }

    // ---- helpers ----

    /** 스레드 20개가 같은 방·같은 시간을 동시에 예약. 최종 CONFIRMED 수를 반환. */
    private long fireSameSlot(Consumer<CreateReservationRequest> reserveFn) throws InterruptedException {
        int threadCount = 20;
        OffsetDateTime start = OffsetDateTime.parse("2026-09-01T10:00:00+09:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-09-01T12:00:00+09:00");
        CreateReservationRequest req = new CreateReservationRequest(ROOM_ID, start, end, "동시성테스터", null, null);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    startGate.await();
                    reserveFn.accept(req);
                    success.incrementAndGet();
                } catch (IllegalStateException e) {
                    conflict.incrementAndGet();
                } catch (Exception ignored) {
                }
            });
        }
        ready.await();
        startGate.countDown();
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        long confirmed = countConfirmed();
        System.out.printf("[동시성-같은슬롯] 성공저장=%d, 충돌거부=%d, DB의 CONFIRMED=%d%n",
                success.get(), conflict.get(), confirmed);
        return confirmed;
    }

    /** 스레드 n개가 서로 다른 시간(안 겹침)을 동시에 예약. 성공 수를 반환. */
    private AtomicInteger fireDifferentSlots(int n) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch ready = new CountDownLatch(n);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < n; i++) {
            final int hour = 8 + i; // 08~09, 09~10, ... 서로 겹치지 않는 1시간 슬롯
            pool.submit(() -> {
                CreateReservationRequest req = new CreateReservationRequest(ROOM_ID,
                        OffsetDateTime.parse(String.format("2026-09-02T%02d:00:00+09:00", hour)),
                        OffsetDateTime.parse(String.format("2026-09-02T%02d:00:00+09:00", hour + 1)),
                        "게스트" + hour, null, null);
                ready.countDown();
                try {
                    startGate.await();
                    reservationService.reserve(req);
                    success.incrementAndGet();
                } catch (Exception ignored) {
                }
            });
        }
        ready.await();
        startGate.countDown();
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
        return success;
    }

    // 활성 예약(취소 제외) 수 — 예약은 생성 시 PENDING이므로 CONFIRMED만 세면 안 된다
    private long countConfirmed() {
        return reservationRepository.findByRoomIdOrderByStartAtAsc(ROOM_ID).stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .count();
    }
}
