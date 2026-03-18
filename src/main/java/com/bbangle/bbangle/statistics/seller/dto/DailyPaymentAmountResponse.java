package com.bbangle.bbangle.statistics.seller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyPaymentAmountResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private StatisticsPeriod period;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long averageAmount;
    private List<DailyPaymentAmountItem> dailyAmounts;

    @Getter
    @AllArgsConstructor
    public static class DailyPaymentAmountItem {

        private LocalDate date;
        private Long amount;
    }
}
