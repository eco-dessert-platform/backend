package com.bbangle.bbangle.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

import java.sql.Date;


@Builder
public record AnalyticsReviewUsageCountDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        Date date,
        Long reviewCount
) {

    @QueryProjection
    public AnalyticsReviewUsageCountDto {
    }

}
