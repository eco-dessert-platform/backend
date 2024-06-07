package com.bbangle.bbangle.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.sql.Date;

@Builder
public record AnalyticsRatioWithDateResponseDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        Date date,
        String ratio
) {
}
