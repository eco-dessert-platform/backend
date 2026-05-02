package com.bbangle.bbangle.statistics.seller.dto;

import com.bbangle.bbangle.statistics.domain.model.StatisticsPeriod;
import java.time.LocalDate;
import java.util.List;

public record WeekdayPaymentAmountResponse(
    LocalDate startDate,
    LocalDate endDate,
    StatisticsPeriod period,
    List<WeekdayPaymentAmountItem> weekdayAmounts
) {

    public record WeekdayPaymentAmountItem(
        Integer weekday,
        Long amount,
        Long averageAmount
    ) {
    }
}
