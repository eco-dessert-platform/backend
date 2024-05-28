package com.bbangle.bbangle.analytics.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.sql.Date;

public record AnalyticsMembersUsingWishlistDto(
    Date date,
    Long count
) {

    @QueryProjection
    public AnalyticsMembersUsingWishlistDto {
    }

}
