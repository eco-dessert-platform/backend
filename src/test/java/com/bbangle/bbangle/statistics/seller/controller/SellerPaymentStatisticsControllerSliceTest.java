package com.bbangle.bbangle.statistics.seller.controller;

import static com.bbangle.bbangle.common.service.ResponseService.CommonResponse.SUCCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.common.adaptor.slack.TestSlackAdaptorConfig;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentAmountResponse;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentAmountResponse.DailyPaymentAmountItem;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentCountResponse;
import com.bbangle.bbangle.statistics.seller.dto.DailyPaymentCountResponse.DailyPaymentCountItem;
import com.bbangle.bbangle.statistics.seller.dto.DailyRefundRateResponse;
import com.bbangle.bbangle.statistics.seller.dto.DailyRefundRateResponse.DailyRefundRateItem;
import com.bbangle.bbangle.statistics.seller.dto.WeekdayPaymentAmountResponse;
import com.bbangle.bbangle.statistics.seller.dto.WeekdayPaymentAmountResponse.WeekdayPaymentAmountItem;
import com.bbangle.bbangle.statistics.seller.service.SellerPaymentStatisticsService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@DisplayName("[컨트롤러] SellerPaymentStatisticsController")
@Import({
    TestSlackAdaptorConfig.class,
    ResponseService.class
})
@WebMvcTest(controllers = SellerPaymentStatisticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class SellerPaymentStatisticsControllerSliceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SellerPaymentStatisticsService sellerPaymentStatisticsService;

    @Test
    @DisplayName("결제 금액 통계를 반환한다")
    void getDailyPaymentAmount_success() throws Exception {
        DailyPaymentAmountResponse response = new DailyPaymentAmountResponse(
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 7),
            StatisticsPeriod.DAY,
            null,
            List.of(
                new DailyPaymentAmountItem(LocalDate.of(2026, 3, 1), 12000L),
                new DailyPaymentAmountItem(LocalDate.of(2026, 3, 2), 0L),
                new DailyPaymentAmountItem(LocalDate.of(2026, 3, 3), 3000L),
                new DailyPaymentAmountItem(LocalDate.of(2026, 3, 4), 0L),
                new DailyPaymentAmountItem(LocalDate.of(2026, 3, 5), 0L),
                new DailyPaymentAmountItem(LocalDate.of(2026, 3, 6), 0L),
                new DailyPaymentAmountItem(LocalDate.of(2026, 3, 7), 0L)
            )
        );

        given(sellerPaymentStatisticsService.getDailyPaymentAmount(any(), any(), any()))
            .willReturn(response);

        mvc.perform(get("/api/v1/seller/payments/statistics/daily-amount")
                .param("date", "2026-03-07")
                .param("period", "DAY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
            .andExpect(jsonPath("$.result.startDate").value("2026-03-01"))
            .andExpect(jsonPath("$.result.endDate").value("2026-03-07"))
            .andExpect(jsonPath("$.result.period").value("DAY"))
            .andExpect(jsonPath("$.result.dailyAmounts.length()").value(7))
            .andExpect(jsonPath("$.result.dailyAmounts[0].amount").value(12000))
            .andExpect(jsonPath("$.result.dailyAmounts[2].amount").value(3000))
            .andExpect(jsonPath("$.result.dailyAmounts[6].amount").value(0));
    }

    @Test
    @DisplayName("결제 건수 통계를 반환한다")
    void getDailyPaymentCount_success() throws Exception {
        DailyPaymentCountResponse response = new DailyPaymentCountResponse(
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 7),
            StatisticsPeriod.DAY,
            null,
            null,
            List.of(
                new DailyPaymentCountItem(LocalDate.of(2026, 3, 1), 1L, 2L),
                new DailyPaymentCountItem(LocalDate.of(2026, 3, 2), 0L, 0L),
                new DailyPaymentCountItem(LocalDate.of(2026, 3, 3), 1L, 1L),
                new DailyPaymentCountItem(LocalDate.of(2026, 3, 4), 0L, 0L),
                new DailyPaymentCountItem(LocalDate.of(2026, 3, 5), 0L, 0L),
                new DailyPaymentCountItem(LocalDate.of(2026, 3, 6), 0L, 0L),
                new DailyPaymentCountItem(LocalDate.of(2026, 3, 7), 0L, 0L)
            )
        );

        given(sellerPaymentStatisticsService.getDailyPaymentCount(any(), any(), any()))
            .willReturn(response);

        mvc.perform(get("/api/v1/seller/payments/statistics/daily-count")
                .param("date", "2026-03-07")
                .param("period", "DAY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
            .andExpect(jsonPath("$.result.startDate").value("2026-03-01"))
            .andExpect(jsonPath("$.result.endDate").value("2026-03-07"))
            .andExpect(jsonPath("$.result.period").value("DAY"))
            .andExpect(jsonPath("$.result.dailyCounts.length()").value(7))
            .andExpect(jsonPath("$.result.dailyCounts[0].buyerCount").value(1))
            .andExpect(jsonPath("$.result.dailyCounts[0].paymentCount").value(2))
            .andExpect(jsonPath("$.result.dailyCounts[2].buyerCount").value(1))
            .andExpect(jsonPath("$.result.dailyCounts[2].paymentCount").value(1));
    }

    @Test
    @DisplayName("요일별 결제 금액 통계를 반환한다")
    void getWeekdayPaymentAmount_success() throws Exception {
        WeekdayPaymentAmountResponse response = new WeekdayPaymentAmountResponse(
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 7),
            StatisticsPeriod.DAY,
            List.of(
                new WeekdayPaymentAmountItem(1, 1000L, 1000L),
                new WeekdayPaymentAmountItem(2, 2000L, 2000L),
                new WeekdayPaymentAmountItem(3, 3000L, 3000L),
                new WeekdayPaymentAmountItem(4, 4000L, 4000L),
                new WeekdayPaymentAmountItem(5, 5000L, 5000L),
                new WeekdayPaymentAmountItem(6, 6000L, 6000L),
                new WeekdayPaymentAmountItem(7, 7000L, 7000L)
            )
        );

        given(sellerPaymentStatisticsService.getWeekdayPaymentAmount(any(), any(), any()))
            .willReturn(response);

        mvc.perform(get("/api/v1/seller/payments/statistics/weekday")
                .param("date", "2026-03-07")
                .param("period", "DAY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
            .andExpect(jsonPath("$.result.startDate").value("2026-03-01"))
            .andExpect(jsonPath("$.result.endDate").value("2026-03-07"))
            .andExpect(jsonPath("$.result.period").value("DAY"))
            .andExpect(jsonPath("$.result.weekdayAmounts.length()").value(7))
            .andExpect(jsonPath("$.result.weekdayAmounts[0].weekday").value(1))
            .andExpect(jsonPath("$.result.weekdayAmounts[0].amount").value(1000))
            .andExpect(jsonPath("$.result.weekdayAmounts[0].averageAmount").value(1000))
            .andExpect(jsonPath("$.result.weekdayAmounts[6].weekday").value(7))
            .andExpect(jsonPath("$.result.weekdayAmounts[6].amount").value(7000))
            .andExpect(jsonPath("$.result.weekdayAmounts[6].averageAmount").value(7000));
    }

    @Test
    @DisplayName("환불율 통계를 반환한다")
    void getDailyRefundRate_success() throws Exception {
        DailyRefundRateResponse response = new DailyRefundRateResponse(
            LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 7),
            StatisticsPeriod.DAY,
            List.of(
                new DailyRefundRateItem(LocalDate.of(2026, 3, 1), 12000L, 3000L, new BigDecimal("25.00")),
                new DailyRefundRateItem(LocalDate.of(2026, 3, 2), 0L, 0L, new BigDecimal("0.00")),
                new DailyRefundRateItem(LocalDate.of(2026, 3, 3), 10000L, 2000L, new BigDecimal("20.00")),
                new DailyRefundRateItem(LocalDate.of(2026, 3, 4), 0L, 0L, new BigDecimal("0.00")),
                new DailyRefundRateItem(LocalDate.of(2026, 3, 5), 0L, 0L, new BigDecimal("0.00")),
                new DailyRefundRateItem(LocalDate.of(2026, 3, 6), 0L, 0L, new BigDecimal("0.00")),
                new DailyRefundRateItem(LocalDate.of(2026, 3, 7), 0L, 0L, new BigDecimal("0.00"))
            )
        );

        given(sellerPaymentStatisticsService.getDailyRefundRate(any(), any(), any()))
            .willReturn(response);

        mvc.perform(get("/api/v1/seller/payments/statistics/daily-refund-rate")
                .param("date", "2026-03-07")
                .param("period", "DAY"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.code").value(SUCCESS.getCode()))
            .andExpect(jsonPath("$.message").value(SUCCESS.getMessage()))
            .andExpect(jsonPath("$.result.startDate").value("2026-03-01"))
            .andExpect(jsonPath("$.result.endDate").value("2026-03-07"))
            .andExpect(jsonPath("$.result.period").value("DAY"))
            .andExpect(jsonPath("$.result.dailyRefundRates.length()").value(7))
            .andExpect(jsonPath("$.result.dailyRefundRates[0].paymentAmount").value(12000))
            .andExpect(jsonPath("$.result.dailyRefundRates[0].refundAmount").value(3000))
            .andExpect(jsonPath("$.result.dailyRefundRates[0].refundRate").value(25.00))
            .andExpect(jsonPath("$.result.dailyRefundRates[2].refundRate").value(20.00));
    }
}
