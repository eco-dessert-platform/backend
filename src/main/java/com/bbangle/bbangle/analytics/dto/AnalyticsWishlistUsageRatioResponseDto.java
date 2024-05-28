package com.bbangle.bbangle.analytics.dto;

import lombok.Builder;

import java.sql.Date;

@Builder
public record AnalyticsWishlistUsageRatioResponseDto(
        Date date,
        String wishlistUsageRatio
) {
}
