package com.spaceflow.pricing;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/**
 * 예약 전 예상 요금 견적 API.
 * 예: GET /api/rooms/1/quote?start=2026-09-05T18:00:00%2B09:00&end=2026-09-05T22:00:00%2B09:00
 * (URL에서 '+'는 공백으로 해석되므로 %2B로 인코딩해야 한다)
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @GetMapping("/{roomId}/quote")
    public PriceQuote quote(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
            @RequestParam(required = false) String coupon) {
        return pricingService.quote(roomId, start, end, coupon);
    }
}
