package com.bbangle.bbangle.statistics.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.statistics.domain.SellerStatisticsDaily;
import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import com.bbangle.bbangle.statistics.repository.SellerStatisticsRepository;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentAmountResponse;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentCountResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[UnitTest] SellerPaymentStatisticsService")
@ExtendWith(MockitoExtension.class)
class SellerPaymentStatisticsServiceUnitTest {

    @InjectMocks
    private SellerPaymentStatisticsService sut;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private SellerStatisticsRepository sellerStatisticsRepository;

    @Test
    @DisplayName("fills missing day buckets with zero for payment amount")
    void getDailyPaymentAmount_fillMissingDatesWithZero() {
        Long sellerId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 3, 7);

        givenExistingSeller(sellerId);

        SellerStatisticsDaily day1 = mockStatisticsDaily(
            LocalDateTime.of(2026, 3, 1, 10, 0),
            12000L,
            0,
            0
        );
        SellerStatisticsDaily day3 = mockStatisticsDaily(
            LocalDateTime.of(2026, 3, 3, 10, 0),
            3000L,
            0,
            0
        );

        given(sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(day1, day3));

        DailyPaymentAmountResponse result = sut.getDailyPaymentAmount(
            sellerId, Optional.of(targetDate), Optional.of(StatisticsPeriod.DAY));

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 3, 7));
        assertThat(result.period()).isEqualTo(StatisticsPeriod.DAY);
        assertThat(result.averageAmount()).isNull();
        assertThat(result.dailyAmounts()).hasSize(7);
        assertThat(result.dailyAmounts().get(0).date()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(result.dailyAmounts().get(0).amount()).isEqualTo(12000L);
        assertThat(result.dailyAmounts().get(1).amount()).isEqualTo(0L);
        assertThat(result.dailyAmounts().get(2).amount()).isEqualTo(3000L);
        assertThat(result.dailyAmounts().get(6).amount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("aggregates weekly payment amount buckets")
    void getDailyPaymentAmount_weeklyBuckets() {
        Long sellerId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 3, 16);

        givenExistingSeller(sellerId);

        SellerStatisticsDaily week1Day1 = mockStatisticsDaily(
            LocalDateTime.of(2026, 2, 2, 10, 0),
            1000L,
            0,
            0
        );
        SellerStatisticsDaily week1Day2 = mockStatisticsDaily(
            LocalDateTime.of(2026, 2, 5, 10, 0),
            2000L,
            0,
            0
        );
        SellerStatisticsDaily week7Day = mockStatisticsDaily(
            LocalDateTime.of(2026, 3, 20, 10, 0),
            7000L,
            0,
            0
        );

        given(sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(week1Day1, week1Day2, week7Day));

        DailyPaymentAmountResponse result = sut.getDailyPaymentAmount(
            sellerId, Optional.of(targetDate), Optional.of(StatisticsPeriod.WEEK));

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 3, 22));
        assertThat(result.period()).isEqualTo(StatisticsPeriod.WEEK);
        assertThat(result.averageAmount()).isEqualTo(1429L);
        assertThat(result.dailyAmounts()).hasSize(7);
        assertThat(result.dailyAmounts().get(0).amount()).isEqualTo(3000L);
        assertThat(result.dailyAmounts().get(6).amount()).isEqualTo(7000L);
    }

    @Test
    @DisplayName("aggregates monthly payment amount buckets")
    void getDailyPaymentAmount_monthlyBuckets() {
        Long sellerId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 3, 10);

        givenExistingSeller(sellerId);

        SellerStatisticsDaily month1Day = mockStatisticsDaily(
            LocalDateTime.of(2025, 9, 2, 10, 0),
            500L,
            0,
            0
        );
        SellerStatisticsDaily month7Day1 = mockStatisticsDaily(
            LocalDateTime.of(2026, 3, 1, 10, 0),
            1000L,
            0,
            0
        );
        SellerStatisticsDaily month7Day2 = mockStatisticsDaily(
            LocalDateTime.of(2026, 3, 15, 10, 0),
            2000L,
            0,
            0
        );

        given(sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(month1Day, month7Day1, month7Day2));

        DailyPaymentAmountResponse result = sut.getDailyPaymentAmount(
            sellerId, Optional.of(targetDate), Optional.of(StatisticsPeriod.MONTH));

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2025, 9, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(result.period()).isEqualTo(StatisticsPeriod.MONTH);
        assertThat(result.averageAmount()).isEqualTo(500L);
        assertThat(result.dailyAmounts()).hasSize(7);
        assertThat(result.dailyAmounts().get(0).amount()).isEqualTo(500L);
        assertThat(result.dailyAmounts().get(6).amount()).isEqualTo(3000L);
    }

    @Test
    @DisplayName("uses DAY as the default period for payment amount")
    void getDailyPaymentAmount_defaultPeriodIsDay() {
        Long sellerId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 3, 7);

        givenExistingSeller(sellerId);
        given(sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of());

        DailyPaymentAmountResponse result = sut.getDailyPaymentAmount(
            sellerId, Optional.of(targetDate), Optional.empty());

        assertThat(result.period()).isEqualTo(StatisticsPeriod.DAY);
        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 3, 7));
        assertThat(result.averageAmount()).isNull();
    }

    @Test
    @DisplayName("aggregates weekly buyer and payment counts")
    void getDailyPaymentCount_weeklyBuckets() {
        Long sellerId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 3, 16);

        givenExistingSeller(sellerId);

        SellerStatisticsDaily week1Day1 = mockStatisticsDaily(
            LocalDateTime.of(2026, 2, 2, 10, 0),
            1000L,
            2,
            2
        );
        SellerStatisticsDaily week1Day2 = mockStatisticsDaily(
            LocalDateTime.of(2026, 2, 5, 10, 0),
            2000L,
            1,
            1
        );
        SellerStatisticsDaily week7Day = mockStatisticsDaily(
            LocalDateTime.of(2026, 3, 20, 10, 0),
            7000L,
            4,
            3
        );

        given(sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(week1Day1, week1Day2, week7Day));

        DailyPaymentCountResponse result = sut.getDailyPaymentCount(
            sellerId, Optional.of(targetDate), Optional.of(StatisticsPeriod.WEEK));

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 3, 22));
        assertThat(result.period()).isEqualTo(StatisticsPeriod.WEEK);
        assertThat(result.averageBuyerCount()).isEqualTo(1L);
        assertThat(result.averagePaymentCount()).isEqualTo(1L);
        assertThat(result.dailyCounts()).hasSize(7);
        assertThat(result.dailyCounts().get(0).buyerCount()).isEqualTo(3L);
        assertThat(result.dailyCounts().get(0).paymentCount()).isEqualTo(3L);
        assertThat(result.dailyCounts().get(6).buyerCount()).isEqualTo(3L);
        assertThat(result.dailyCounts().get(6).paymentCount()).isEqualTo(4L);
    }

    @Test
    @DisplayName("fills missing day buckets with zero for payment count")
    void getDailyPaymentCount_fillMissingDatesWithZero() {
        Long sellerId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 3, 7);

        givenExistingSeller(sellerId);

        SellerStatisticsDaily day1 = mockStatisticsDaily(
            LocalDateTime.of(2026, 3, 1, 10, 0),
            12000L,
            2,
            1
        );
        SellerStatisticsDaily day3 = mockStatisticsDaily(
            LocalDateTime.of(2026, 3, 3, 10, 0),
            3000L,
            1,
            1
        );

        given(sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(day1, day3));

        DailyPaymentCountResponse result = sut.getDailyPaymentCount(
            sellerId, Optional.of(targetDate), Optional.of(StatisticsPeriod.DAY));

        assertThat(result.averageBuyerCount()).isNull();
        assertThat(result.averagePaymentCount()).isNull();
        assertThat(result.dailyCounts()).hasSize(7);
        assertThat(result.dailyCounts().get(0).buyerCount()).isEqualTo(1L);
        assertThat(result.dailyCounts().get(0).paymentCount()).isEqualTo(2L);
        assertThat(result.dailyCounts().get(1).buyerCount()).isEqualTo(0L);
        assertThat(result.dailyCounts().get(1).paymentCount()).isEqualTo(0L);
        assertThat(result.dailyCounts().get(2).buyerCount()).isEqualTo(1L);
        assertThat(result.dailyCounts().get(2).paymentCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("throws SELLER_NOT_FOUND when seller does not exist")
    void getDailyPaymentAmount_sellerNotFound() {
        Long sellerId = 999L;
        given(sellerRepository.findById(sellerId)).willReturn(Optional.empty());

        BbangleException result = assertThrows(
            BbangleException.class,
            () -> sut.getDailyPaymentAmount(sellerId, Optional.empty(), Optional.empty()));

        assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.SELLER_NOT_FOUND);
    }

    private void givenExistingSeller(Long sellerId) {
        Seller seller = org.mockito.Mockito.mock(Seller.class);
        given(seller.getId()).willReturn(sellerId);
        given(sellerRepository.findById(sellerId)).willReturn(Optional.of(seller));
    }

    private SellerStatisticsDaily mockStatisticsDaily(
        LocalDateTime statDate,
        long amount,
        int totalOrders,
        int totalBuyers
    ) {
        SellerStatisticsDaily row = org.mockito.Mockito.mock(SellerStatisticsDaily.class);
        given(row.getStatDate()).willReturn(statDate);
        given(row.getTotalAmount()).willReturn(BigDecimal.valueOf(amount));
        given(row.getTotalOrdersCount()).willReturn((long) totalOrders);
        given(row.getTotalBuyersCount()).willReturn((long) totalBuyers);
        return row;
    }
}
