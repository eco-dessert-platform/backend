package com.bbangle.bbangle.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Date;


public record AnalyticsReviewUsageCountResponseDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        Date date,
        String reviewUsageRatio
) {
}
