package com.bbangle.bbangle.statistics.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.statistics.domain.SellerStatisticsDaily;
import com.bbangle.bbangle.statistics.repository.SellerStatisticsRepository;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentAmountResponse;
import com.bbangle.bbangle.statistics.seller.dto.StatisticsPeriod;
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
    @DisplayName("DAY 조회는 기준일 포함 최근 7일 데이터를 0으로 채워 반환한다")
    void getDailyPaymentAmount_fillMissingDatesWithZero() {
        // given
        Long sellerId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 3, 7);

        given(sellerRepository.existsById(sellerId)).willReturn(true);

        SellerStatisticsDaily day1 = org.mockito.Mockito.mock(SellerStatisticsDaily.class);
        given(day1.getStatDate()).willReturn(LocalDateTime.of(2026, 3, 1, 10, 0));
        given(day1.getTotalAmount()).willReturn(BigDecimal.valueOf(12000));

        SellerStatisticsDaily day3 = org.mockito.Mockito.mock(SellerStatisticsDaily.class);
        given(day3.getStatDate()).willReturn(LocalDateTime.of(2026, 3, 3, 10, 0));
        given(day3.getTotalAmount()).willReturn(BigDecimal.valueOf(3000));

        given(sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(day1, day3));

        // when
        DailyPaymentAmountResponse result = sut.getDailyPaymentAmount(
            sellerId, Optional.of(targetDate), Optional.of(StatisticsPeriod.DAY));

        // then
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 7));
        assertThat(result.getPeriod()).isEqualTo(StatisticsPeriod.DAY);
        assertThat(result.getAverageAmount()).isNull();
        assertThat(result.getDailyAmounts()).hasSize(7);
        assertThat(result.getDailyAmounts().get(0).getDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(result.getDailyAmounts().get(0).getAmount()).isEqualTo(12000L);
        assertThat(result.getDailyAmounts().get(1).getDate()).isEqualTo(LocalDate.of(2026, 3, 2));
        assertThat(result.getDailyAmounts().get(1).getAmount()).isEqualTo(0L);
        assertThat(result.getDailyAmounts().get(2).getDate()).isEqualTo(LocalDate.of(2026, 3, 3));
        assertThat(result.getDailyAmounts().get(2).getAmount()).isEqualTo(3000L);
        assertThat(result.getDailyAmounts().get(6).getDate()).isEqualTo(LocalDate.of(2026, 3, 7));
        assertThat(result.getDailyAmounts().get(6).getAmount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("WEEK 조회는 기준일 포함 최근 7주 합계를 반환한다")
    void getDailyPaymentAmount_weeklyBuckets() {
        // given
        Long sellerId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 3, 16);

        given(sellerRepository.existsById(sellerId)).willReturn(true);

        SellerStatisticsDaily week1Day1 = org.mockito.Mockito.mock(SellerStatisticsDaily.class);
        given(week1Day1.getStatDate()).willReturn(LocalDateTime.of(2026, 2, 2, 10, 0));
        given(week1Day1.getTotalAmount()).willReturn(BigDecimal.valueOf(1000));

        SellerStatisticsDaily week1Day2 = org.mockito.Mockito.mock(SellerStatisticsDaily.class);
        given(week1Day2.getStatDate()).willReturn(LocalDateTime.of(2026, 2, 5, 10, 0));
        given(week1Day2.getTotalAmount()).willReturn(BigDecimal.valueOf(2000));

        SellerStatisticsDaily week7Day = org.mockito.Mockito.mock(SellerStatisticsDaily.class);
        given(week7Day.getStatDate()).willReturn(LocalDateTime.of(2026, 3, 20, 10, 0));
        given(week7Day.getTotalAmount()).willReturn(BigDecimal.valueOf(7000));

        given(sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(week1Day1, week1Day2, week7Day));

        // when
        DailyPaymentAmountResponse result = sut.getDailyPaymentAmount(
            sellerId, Optional.of(targetDate), Optional.of(StatisticsPeriod.WEEK));

        // then
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 22));
        assertThat(result.getPeriod()).isEqualTo(StatisticsPeriod.WEEK);
        assertThat(result.getAverageAmount()).isEqualTo(1429L);
        assertThat(result.getDailyAmounts()).hasSize(7);
        assertThat(result.getDailyAmounts().get(0).getDate()).isEqualTo(LocalDate.of(2026, 2, 2));
        assertThat(result.getDailyAmounts().get(0).getAmount()).isEqualTo(3000L);
        assertThat(result.getDailyAmounts().get(6).getDate()).isEqualTo(LocalDate.of(2026, 3, 16));
        assertThat(result.getDailyAmounts().get(6).getAmount()).isEqualTo(7000L);
    }

    @Test
    @DisplayName("MONTH 조회는 기준월 포함 최근 7개월 합계를 반환한다")
    void getDailyPaymentAmount_monthlyBuckets() {
        // given
        Long sellerId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 3, 10);

        given(sellerRepository.existsById(sellerId)).willReturn(true);

        SellerStatisticsDaily month1Day = org.mockito.Mockito.mock(SellerStatisticsDaily.class);
        given(month1Day.getStatDate()).willReturn(LocalDateTime.of(2025, 9, 2, 10, 0));
        given(month1Day.getTotalAmount()).willReturn(BigDecimal.valueOf(500));

        SellerStatisticsDaily month7Day1 = org.mockito.Mockito.mock(SellerStatisticsDaily.class);
        given(month7Day1.getStatDate()).willReturn(LocalDateTime.of(2026, 3, 1, 10, 0));
        given(month7Day1.getTotalAmount()).willReturn(BigDecimal.valueOf(1000));

        SellerStatisticsDaily month7Day2 = org.mockito.Mockito.mock(SellerStatisticsDaily.class);
        given(month7Day2.getStatDate()).willReturn(LocalDateTime.of(2026, 3, 15, 10, 0));
        given(month7Day2.getTotalAmount()).willReturn(BigDecimal.valueOf(2000));

        given(sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of(month1Day, month7Day1, month7Day2));

        // when
        DailyPaymentAmountResponse result = sut.getDailyPaymentAmount(
            sellerId, Optional.of(targetDate), Optional.of(StatisticsPeriod.MONTH));

        // then
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 9, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(result.getPeriod()).isEqualTo(StatisticsPeriod.MONTH);
        assertThat(result.getAverageAmount()).isEqualTo(500L);
        assertThat(result.getDailyAmounts()).hasSize(7);
        assertThat(result.getDailyAmounts().get(0).getDate()).isEqualTo(LocalDate.of(2025, 9, 1));
        assertThat(result.getDailyAmounts().get(0).getAmount()).isEqualTo(500L);
        assertThat(result.getDailyAmounts().get(6).getDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(result.getDailyAmounts().get(6).getAmount()).isEqualTo(3000L);
    }

    @Test
    @DisplayName("기본 period는 DAY다")
    void getDailyPaymentAmount_defaultPeriodIsDay() {
        // given
        Long sellerId = 1L;
        LocalDate targetDate = LocalDate.of(2026, 3, 7);

        given(sellerRepository.existsById(sellerId)).willReturn(true);
        given(sellerStatisticsRepository.findBySellerIdAndStatDateBetweenOrderByStatDateAsc(
            eq(sellerId), any(LocalDateTime.class), any(LocalDateTime.class)
        )).willReturn(List.of());

        // when
        DailyPaymentAmountResponse result = sut.getDailyPaymentAmount(
            sellerId, Optional.of(targetDate), Optional.empty());

        // then
        assertThat(result.getPeriod()).isEqualTo(StatisticsPeriod.DAY);
        assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2026, 3, 7));
        assertThat(result.getAverageAmount()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 판매자는 SELLER_NOT_FOUND 예외가 발생한다")
    void getDailyPaymentAmount_sellerNotFound() {
        // given
        Long sellerId = 999L;
        given(sellerRepository.existsById(sellerId)).willReturn(false);

        // when
        BbangleException result = assertThrows(
            BbangleException.class,
            () -> sut.getDailyPaymentAmount(sellerId, Optional.empty(), Optional.empty()));

        // then
        assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.SELLER_NOT_FOUND);
    }
}
