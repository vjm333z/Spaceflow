package com.spaceflow.reservation;

import com.spaceflow.TestcontainersConfiguration;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 동시성 재현 테스트.
 * 스레드 여러 개가 "같은 방·같은 시간"을 동시에 예약하도록 만들어, 순진한 버전(check-then-act)이
 * 중복 예약을 허용한다는 것을 증명한다. (락을 걸면 기대값을 '정확히 1건'으로 뒤집는다.)
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReservationConcurrencyTest {

    @Autowired
    ReservationService reservationService;
    @Autowired
    ReservationRepository reservationRepository;

    @Test
    void 동시에_같은_시간을_예약하면_중복이_생긴다() throws InterruptedException {
        int threadCount = 20;
        OffsetDateTime start = OffsetDateTime.parse("2026-09-01T10:00:00+09:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-09-01T12:00:00+09:00");
        CreateReservationRequest req = new CreateReservationRequest(1L, start, end, "동시성테스터", null);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount); // 모든 스레드가 준비됐는지
        CountDownLatch startGate = new CountDownLatch(1);        // "출발!" 신호 (동시에 풀기)
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    startGate.await();              // 전원 여기서 대기하다 동시에 출발
                    reservationService.reserve(req);
                    success.incrementAndGet();
                } catch (IllegalStateException e) {
                    conflict.incrementAndGet();     // "이미 예약된 시간대" 로 거부된 경우
                } catch (Exception ignored) {
                    // DB 제약 위반 등 기타
                }
            });
        }

        ready.await();          // 전원 준비될 때까지
        startGate.countDown();  // 동시에 출발
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        long confirmed = reservationRepository.findByRoomIdOrderByStartAtAsc(1L).stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .count();

        System.out.printf("[동시성 결과] 성공저장=%d, 충돌거부=%d, DB의 CONFIRMED 예약=%d%n",
                success.get(), conflict.get(), confirmed);

        // 순진한 버전은 방어가 없어 중복이 생긴다 (정상이라면 1이어야 함)
        assertThat(confirmed).isGreaterThan(1);
    }
}
