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
import com.bbangle.bbangle.statistics.seller.dto.DailyRefundRateResponse;
import com.bbangle.bbangle.statistics.seller.dto.WeekdayPaymentAmountResponse;
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

@DisplayName("[단위 테스트] SellerPaymentStatisticsService")
@ExtendWith(MockitoExtension.class)
class SellerPaymentStatisticsServiceUnitTest {

    @InjectMocks
    private SellerPaymentStatisticsService sut;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private SellerStatisticsRepository sellerStatisticsRepository;

    @Test
    @DisplayName("결제 금액 조회 시 비어 있는 날짜 구간은 0으로 채운다")
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
    @DisplayName("주간 결제 금액 구간을 집계한다")
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
    @DisplayName("월간 결제 금액 구간을 집계한다")
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
    @DisplayName("결제 금액 조회 시 기본 기간은 DAY를 사용한다")
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
    @DisplayName("결제 금액을 요일별로 집계하고 요일 평균 금액을 계산한다")
    void getWeekdayPaymentAmount_success() {
        Long sellerId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 3, 16);

        givenExistingSeller(sellerId);

        SellerStatisticsDaily monday1 = mockStatisticsDaily(
            LocalDateTime.of(2026, 2, 2, 10, 0),
            1000L,
            0,
            0
        );
        SellerStatisticsDaily monday2 = mockStatisticsDaily(
            LocalDateTime.of(2026, 3, 16, 10, 0),
            3000L,
            0,
            0
        );
        SellerStatisticsDaily thursday = mockStatisticsDaily(
            LocalDateTime.of(2026, 2, 5, 10, 0),
            2000L,
            0,
            0
        );
        SellerStatisticsDaily friday = mockStatisticsDaily(
            LocalDateTime.of(2026, 3, 20, 10, 0),
            7000L,
            0,
            0
        );

        given(sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(monday1, monday2, thursday, friday));

        WeekdayPaymentAmountResponse result = sut.getWeekdayPaymentAmount(
            sellerId, Optional.of(targetDate), Optional.of(StatisticsPeriod.WEEK));

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 3, 22));
        assertThat(result.period()).isEqualTo(StatisticsPeriod.WEEK);
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
        assertThat(result.weekdayAmounts().get(6).amount()).isEqualTo(0L);
        assertThat(result.weekdayAmounts().get(6).averageAmount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("주간 구매자 수와 결제 건수를 집계한다")
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
    @DisplayName("결제 건수 조회 시 비어 있는 날짜 구간은 0으로 채운다")
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
    @DisplayName("환불율 통계는 statistics 적재 데이터 기준으로 버킷 집계한다")
    void getDailyRefundRate_success() {
        Long sellerId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 3, 16);

        givenExistingSeller(sellerId);

        SellerStatisticsDaily week1Day1 = mockStatisticsDaily(
            LocalDateTime.of(2026, 2, 2, 10, 0),
            10000L,
            0,
            0,
            2000L
        );
        SellerStatisticsDaily week1Day2 = mockStatisticsDaily(
            LocalDateTime.of(2026, 2, 5, 10, 0),
            5000L,
            0,
            0,
            1000L
        );
        SellerStatisticsDaily week7Day = mockStatisticsDaily(
            LocalDateTime.of(2026, 3, 20, 10, 0),
            18000L,
            0,
            0,
            5000L
        );

        given(sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(week1Day1, week1Day2, week7Day));

        DailyRefundRateResponse result = sut.getDailyRefundRate(
            sellerId, Optional.of(targetDate), Optional.of(StatisticsPeriod.WEEK));

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 3, 22));
        assertThat(result.period()).isEqualTo(StatisticsPeriod.WEEK);
        assertThat(result.dailyRefundRates()).hasSize(7);
        assertThat(result.dailyRefundRates().get(0).paymentAmount()).isEqualTo(15000L);
        assertThat(result.dailyRefundRates().get(0).refundAmount()).isEqualTo(3000L);
        assertThat(result.dailyRefundRates().get(0).refundRate()).isEqualByComparingTo("20.00");
        assertThat(result.dailyRefundRates().get(6).paymentAmount()).isEqualTo(18000L);
        assertThat(result.dailyRefundRates().get(6).refundAmount()).isEqualTo(5000L);
        assertThat(result.dailyRefundRates().get(6).refundRate()).isEqualByComparingTo("27.78");
        assertThat(result.averageRefundRate()).isEqualByComparingTo("6.83");
    }

    @Test
    @DisplayName("판매자가 없으면 SELLER_NOT_FOUND 예외를 던진다")
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
        return mockStatisticsDaily(statDate, amount, totalOrders, totalBuyers, 0L);
    }

    private SellerStatisticsDaily mockStatisticsDaily(
        LocalDateTime statDate,
        long amount,
        int totalOrders,
        int totalBuyers,
        long refundAmount
    ) {
        return SellerStatisticsDaily.create(
            statDate,
            statDate.getDayOfWeek().getValue(),
            BigDecimal.valueOf(amount),
            totalOrders,
            totalBuyers,
            BigDecimal.valueOf(refundAmount),
            refundAmount > 0 ? 1 : 0,
            BigDecimal.ZERO,
            null
        );
    }
}
