package com.bbangle.bbangle.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Date;

public record AnalyticsAccumulationResponseDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        Date date,
        Long count
) {
}
