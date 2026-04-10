package com.bbangle.bbangle.statistics.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[UnitTest] SellerStatisticsDaily")
class SellerStatisticsDailyUnitTest {

    @Test
    @DisplayName("creates a daily statistics entity")
    void create() {
        Seller seller = SellerFixture.defaultSeller();
        LocalDateTime statDate = LocalDateTime.of(2026, 3, 7, 0, 0);

        SellerStatisticsDaily result = SellerStatisticsDaily.create(
            statDate,
            6,
            BigDecimal.valueOf(15000),
            4,
            3,
            BigDecimal.ZERO,
            0,
            BigDecimal.ZERO,
            seller
        );

        assertThat(result.getStatDate()).isEqualTo(statDate);
        assertThat(result.getWeekday()).isEqualTo(6);
        assertThat(result.getTotalAmount()).isEqualByComparingTo("15000");
        assertThat(result.getTotalOrders()).isEqualTo(4);
        assertThat(result.getTotalBuyers()).isEqualTo(3);
        assertThat(result.getSeller()).isEqualTo(seller);
    }

    @Test
    @DisplayName("returns zero when counts are null")
    void nullCountsAreZero() {
        SellerStatisticsDaily result = SellerStatisticsDaily.create(
            LocalDateTime.of(2026, 3, 7, 0, 0),
            6,
            BigDecimal.ZERO,
            null,
            null,
            BigDecimal.ZERO,
            0,
            BigDecimal.ZERO,
            SellerFixture.defaultSeller()
        );

        assertThat(result.getTotalOrdersCount()).isZero();
        assertThat(result.getTotalBuyersCount()).isZero();
    }
}
