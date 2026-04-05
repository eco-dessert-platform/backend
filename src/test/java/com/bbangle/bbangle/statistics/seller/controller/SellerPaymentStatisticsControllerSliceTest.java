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
import com.bbangle.bbangle.statistics.seller.service.SellerPaymentStatisticsService;
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
    @DisplayName("결제 금액 통계 조회 성공")
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
}
