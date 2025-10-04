package com.bbangle.bbangle.analytics.admin.dto;

import java.util.List;

public record AnalyticsCreatedWithinPeriodResponseDto(
    List<DateAndCountDto> dateAndCount,
    Long total,
    String average
) {

    public static AnalyticsCreatedWithinPeriodResponseDto from(List<DateAndCountDto> results, Long total, String average) {
        return new AnalyticsCreatedWithinPeriodResponseDto(results, total, average);
    }

}
