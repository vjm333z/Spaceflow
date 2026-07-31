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
 * 동시성 비교 테스트.
 * 스레드 여러 개가 "같은 방·같은 시간"을 동시에 예약하도록 만들어, 방어 방식별 결과를 확인한다.
 * - 순진한 버전(check-then-act): 중복이 생긴다
 * - 비관적 락: 딱 1건만 성공한다
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
        reservationRepository.deleteAll(); // 테스트 간 격리
    }

    @Test
    void 순진한_버전은_동시예약시_중복이_생긴다() throws InterruptedException {
        long confirmed = fireConcurrently(reservationService::reserve);
        // 방어가 없어 중복 발생 (정상이라면 1이어야 함)
        assertThat(confirmed).isGreaterThan(1);
    }

    @Test
    void 비관적_락_버전은_동시예약해도_1건만_성공한다() throws InterruptedException {
        long confirmed = fireConcurrently(reservationService::reserveWithPessimisticLock);
        assertThat(confirmed).isEqualTo(1);
    }

    @Test
    void 낙관적_락_버전은_동시예약해도_1건만_성공한다() throws InterruptedException {
        long confirmed = fireConcurrently(reservationService::reserveWithOptimisticLock);
        assertThat(confirmed).isEqualTo(1);
    }

    /**
     * 스레드 20개가 같은 방·같은 시간을 동시에 예약하도록 발사하고,
     * 최종적으로 DB에 남은 CONFIRMED 예약 수를 돌려준다.
     */
    private long fireConcurrently(Consumer<CreateReservationRequest> reserveFn) throws InterruptedException {
        int threadCount = 20;
        OffsetDateTime start = OffsetDateTime.parse("2026-09-01T10:00:00+09:00");
        OffsetDateTime end = OffsetDateTime.parse("2026-09-01T12:00:00+09:00");
        CreateReservationRequest req = new CreateReservationRequest(ROOM_ID, start, end, "동시성테스터", null);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount); // 전원 준비됐는지
        CountDownLatch startGate = new CountDownLatch(1);        // "출발!" 신호
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    startGate.await();          // 전원 대기하다 동시에 출발
                    reserveFn.accept(req);
                    success.incrementAndGet();
                } catch (IllegalStateException e) {
                    conflict.incrementAndGet(); // "이미 예약된 시간대"로 거부됨
                } catch (Exception ignored) {
                    // DB 제약 위반 등 기타
                }
            });
        }

        ready.await();
        startGate.countDown();  // 동시에 출발
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);

        long confirmed = reservationRepository.findByRoomIdOrderByStartAtAsc(ROOM_ID).stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED)
                .count();

        System.out.printf("[동시성] 성공저장=%d, 충돌거부=%d, DB의 CONFIRMED=%d%n",
                success.get(), conflict.get(), confirmed);
        return confirmed;
    }
}
