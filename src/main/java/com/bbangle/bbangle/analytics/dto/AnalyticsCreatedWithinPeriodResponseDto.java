package com.bbangle.bbangle.analytics.dto;

import java.util.List;

public record AnalyticsCreatedWithinPeriodResponseDto(
        List<DateAndCountDto> dateAndCount,
        Long total,
        String average
) {

    public static AnalyticsCreatedWithinPeriodResponseDto from(AnalyticsCreatedWithinPeriodResponseDto analyticsCountWithDateResponseDto) {
        return new AnalyticsCreatedWithinPeriodResponseDto(
                analyticsCountWithDateResponseDto.dateAndCount(),
                analyticsCountWithDateResponseDto.total(),
                analyticsCountWithDateResponseDto.average()
        );
    }

}
