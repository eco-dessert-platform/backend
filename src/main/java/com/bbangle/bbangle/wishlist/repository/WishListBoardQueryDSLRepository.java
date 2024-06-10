package com.bbangle.bbangle.wishlist.repository;

import com.bbangle.bbangle.analytics.dto.AnalyticsCreatedWithinPeriodResponseDto;

import java.time.LocalDate;

public interface WishListBoardQueryDSLRepository {

    AnalyticsCreatedWithinPeriodResponseDto countWishlistCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate);
}
