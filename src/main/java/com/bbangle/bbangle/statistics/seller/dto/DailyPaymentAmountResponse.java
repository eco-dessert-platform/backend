package com.bbangle.bbangle.statistics.seller.dto;

import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;

public record DailyPaymentAmountResponse(
    LocalDate startDate,
    LocalDate endDate,
    StatisticsPeriod period,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Long averageAmount,
    List<DailyPaymentAmountItem> dailyAmounts
) {

    public record DailyPaymentAmountItem(
        LocalDate date,
        Long amount
    ) {
    }
}
