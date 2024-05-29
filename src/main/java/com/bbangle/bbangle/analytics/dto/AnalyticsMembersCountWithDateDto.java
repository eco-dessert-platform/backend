package com.bbangle.bbangle.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;

import java.sql.Date;

public record AnalyticsMembersCountWithDateDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        Date date,
        Long count
) {

    @QueryProjection
    public AnalyticsMembersCountWithDateDto {
    }

}
