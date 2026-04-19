package com.bbangle.bbangle.statistics.seller.dto;

import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;

public record DailyPaymentCountResponse(
    LocalDate startDate,
    LocalDate endDate,
    StatisticsPeriod period,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Long averageBuyerCount,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Long averagePaymentCount,
    List<DailyPaymentCountItem> dailyCounts
) {

    public record DailyPaymentCountItem(
        LocalDate date,
        Long buyerCount,
        Long paymentCount
    ) {
    }
}
