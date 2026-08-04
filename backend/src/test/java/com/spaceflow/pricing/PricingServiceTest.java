package com.spaceflow.pricing;

import com.spaceflow.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 요금 엔진 검증. room 1 = 기본 10000원/시간, 규칙: 피크(18~22) +50%, 주말 +30%, 4시간+ -10%.
 * (2026-09-01=화요일 평일, 2026-09-05=토요일 주말)
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PricingServiceTest {

    private static final Long ROOM_ID = 1L;

    @Autowired
    PricingService pricingService;

    private PriceQuote quote(String start, String end) {
        return pricingService.quote(ROOM_ID, OffsetDateTime.parse(start), OffsetDateTime.parse(end));
    }

    @Test
    void 평일_낮_2시간_기본요금만() {
        PriceQuote q = quote("2026-09-01T10:00:00+09:00", "2026-09-01T12:00:00+09:00");
        assertThat(q.total()).isEqualByComparingTo("20000"); // 10000 × 2
        assertThat(q.lines()).isEmpty();
    }

    @Test
    void 평일_저녁_2시간_피크50퍼센트() {
        PriceQuote q = quote("2026-09-01T18:00:00+09:00", "2026-09-01T20:00:00+09:00");
        assertThat(q.total()).isEqualByComparingTo("30000"); // 20000 × 1.5
    }

    @Test
    void 토요일_낮_2시간_주말30퍼센트() {
        PriceQuote q = quote("2026-09-05T10:00:00+09:00", "2026-09-05T12:00:00+09:00");
        assertThat(q.total()).isEqualByComparingTo("26000"); // 20000 × 1.3
    }

    @Test
    void 평일_4시간_장시간할인10퍼센트() {
        PriceQuote q = quote("2026-09-01T10:00:00+09:00", "2026-09-01T14:00:00+09:00");
        assertThat(q.total()).isEqualByComparingTo("36000"); // 40000 × 0.9
    }

    @Test
    void 토요일_저녁_4시간_피크_주말_장시간_복합() {
        PriceQuote q = quote("2026-09-05T18:00:00+09:00", "2026-09-05T22:00:00+09:00");
        // 40000 → 피크 ×1.5=60000 → 주말 ×1.3=78000 → 장시간 ×0.9=70200
        assertThat(q.total()).isEqualByComparingTo("70200");
        assertThat(q.lines()).hasSize(3);
    }

    @Test
    void 기본요금은_시간당_10000원() {
        PriceQuote q = quote("2026-09-01T10:00:00+09:00", "2026-09-01T12:00:00+09:00");
        assertThat(q.basePricePerHour()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(q.hours()).isEqualByComparingTo("2");
    }
}
