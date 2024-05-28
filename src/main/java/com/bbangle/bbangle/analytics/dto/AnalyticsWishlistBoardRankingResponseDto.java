package com.bbangle.bbangle.analytics.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

@Builder
public record AnalyticsWishlistBoardRankingResponseDto(
    Long id,
    String title,
    int price,
    boolean status,
    String profile,
    String purchaseUrl,
    int view,
    int wishCnt
) {

    @QueryProjection
    public AnalyticsWishlistBoardRankingResponseDto {
    }

}
