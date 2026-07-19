package com.bbangle.bbangle.statistics.seller.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.statistics.domain.SellerStatisticsDailyFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import com.bbangle.bbangle.statistics.repository.SellerStatisticsRepository;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentCountResponse;
import com.bbangle.bbangle.statistics.seller.dto.WeekdayPaymentAmountResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합 테스트] SellerPaymentStatisticsService")
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
    @DisplayName("주간 결제 건수를 7개 구간으로 집계한다")
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

    @Test
    @DisplayName("결제 금액을 요일별로 집계한다")
    void getWeekdayPaymentAmount_weeklyBuckets() {
        Seller seller = sellerRepository.saveAndFlush(SellerFixture.defaultSeller());

        sellerStatisticsRepository.saveAndFlush(SellerStatisticsDailyFixture.create(
            seller,
            LocalDateTime.of(2026, 2, 2, 0, 0),
            0,
            0,
            1000
        ));
        sellerStatisticsRepository.saveAndFlush(SellerStatisticsDailyFixture.create(
            seller,
            LocalDateTime.of(2026, 2, 5, 0, 0),
            0,
            0,
            2000
        ));
        sellerStatisticsRepository.saveAndFlush(SellerStatisticsDailyFixture.create(
            seller,
            LocalDateTime.of(2026, 3, 16, 0, 0),
            0,
            0,
            3000
        ));
        sellerStatisticsRepository.saveAndFlush(SellerStatisticsDailyFixture.create(
            seller,
            LocalDateTime.of(2026, 3, 20, 0, 0),
            0,
            0,
            7000
        ));

        WeekdayPaymentAmountResponse result = sellerPaymentStatisticsService.getWeekdayPaymentAmount(
            seller.getId(),
            Optional.of(LocalDate.of(2026, 3, 16)),
            Optional.of(StatisticsPeriod.WEEK)
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 3, 22));
        assertThat(result.weekdayAmounts()).hasSize(7);
        assertThat(result.weekdayAmounts().get(0).weekday()).isEqualTo(1);
        assertThat(result.weekdayAmounts().get(0).amount()).isEqualTo(4000L);
        assertThat(result.weekdayAmounts().get(0).averageAmount()).isEqualTo(571L);
        assertThat(result.weekdayAmounts().get(3).weekday()).isEqualTo(4);
        assertThat(result.weekdayAmounts().get(3).amount()).isEqualTo(2000L);
        assertThat(result.weekdayAmounts().get(3).averageAmount()).isEqualTo(286L);
        assertThat(result.weekdayAmounts().get(4).weekday()).isEqualTo(5);
        assertThat(result.weekdayAmounts().get(4).amount()).isEqualTo(7000L);
        assertThat(result.weekdayAmounts().get(4).averageAmount()).isEqualTo(1000L);
    }
}
