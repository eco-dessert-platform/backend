package com.bbangle.bbangle.fixture.statistics.domain;

import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.statistics.domain.SellerStatisticsDaily;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class SellerStatisticsDailyFixture {

    private SellerStatisticsDailyFixture() {
    }

    public static SellerStatisticsDaily create(
        Seller seller,
        LocalDateTime statDate,
        int totalOrders,
        int totalBuyers,
        long totalAmount
    ) {
        return SellerStatisticsDaily.create(
            statDate,
            statDate.getDayOfWeek().getValue(),
            BigDecimal.valueOf(totalAmount),
            totalOrders,
            totalBuyers,
            BigDecimal.ZERO,
            0,
            BigDecimal.ZERO,
            seller
        );
    }
}
