package com.bbangle.bbangle.wishlist.repository;

import com.bbangle.bbangle.analytics.dto.AnalyticsCountWithDateResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface WishListBoardQueryDSLRepository {

    List<AnalyticsCountWithDateResponseDto> countMembersUsingWishlist(LocalDate startLocalDate, LocalDate endLocalDate);

    List<AnalyticsCountWithDateResponseDto> countWishlistByPeriod(LocalDate startLocalDate, LocalDate endLocalDate);
}
