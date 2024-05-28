package com.bbangle.bbangle.analytics.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.sql.Date;

public record AnalyticsMembersCountWithDateDto(
    Date date,
    Long count
) {

    @QueryProjection
    public AnalyticsMembersCountWithDateDto {
    }

}
