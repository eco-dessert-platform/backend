package com.bbangle.bbangle.wishlist.repository;

import com.bbangle.bbangle.analytics.dto.AnalyticsMembersUsingWishlistDto;
import com.bbangle.bbangle.analytics.dto.AnalyticsWishlistUsageCountResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface WishListBoardQueryDSLRepository {

    List<AnalyticsMembersUsingWishlistDto> countMembersUsingWishlist(LocalDate startLocalDate, LocalDate endLocalDate);

    List<AnalyticsWishlistUsageCountResponseDto> countWishlistByPeriod(LocalDate startLocalDate, LocalDate endLocalDate);
}
