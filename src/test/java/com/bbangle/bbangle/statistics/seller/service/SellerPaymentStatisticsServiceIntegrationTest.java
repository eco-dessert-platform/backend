package com.bbangle.bbangle.statistics.seller.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.statistics.domain.SellerStatisticsDailyFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import com.bbangle.bbangle.statistics.repository.SellerStatisticsRepository;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentCountResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[IntegrationTest] SellerPaymentStatisticsService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SellerPaymentStatisticsServiceIntegrationTest {

    @Autowired
    private SellerPaymentStatisticsService sellerPaymentStatisticsService;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private SellerStatisticsRepository sellerStatisticsRepository;

    @Test
    @DisplayName("aggregates weekly payment counts into seven buckets")
    void getDailyPaymentCount_weeklyBuckets() {
        Seller seller = sellerRepository.saveAndFlush(SellerFixture.defaultSeller());

        sellerStatisticsRepository.saveAndFlush(SellerStatisticsDailyFixture.create(
            seller,
            LocalDateTime.of(2026, 2, 2, 0, 0),
            2,
            2,
            10000
        ));
        sellerStatisticsRepository.saveAndFlush(SellerStatisticsDailyFixture.create(
            seller,
            LocalDateTime.of(2026, 2, 5, 0, 0),
            1,
            1,
            5000
        ));
        sellerStatisticsRepository.saveAndFlush(SellerStatisticsDailyFixture.create(
            seller,
            LocalDateTime.of(2026, 3, 20, 0, 0),
            4,
            3,
            21000
        ));

        DailyPaymentCountResponse result = sellerPaymentStatisticsService.getDailyPaymentCount(
            seller.getId(),
            Optional.of(LocalDate.of(2026, 3, 16)),
            Optional.of(StatisticsPeriod.WEEK)
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 3, 22));
        assertThat(result.averageBuyerCount()).isEqualTo(1L);
        assertThat(result.averagePaymentCount()).isEqualTo(1L);
        assertThat(result.dailyCounts()).hasSize(7);
        assertThat(result.dailyCounts().get(0).date()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(result.dailyCounts().get(0).buyerCount()).isEqualTo(3L);
        assertThat(result.dailyCounts().get(0).paymentCount()).isEqualTo(3L);
        assertThat(result.dailyCounts().get(6).date()).isEqualTo(LocalDate.of(2026, 3, 16));
        assertThat(result.dailyCounts().get(6).buyerCount()).isEqualTo(3L);
        assertThat(result.dailyCounts().get(6).paymentCount()).isEqualTo(4L);
    }
}
