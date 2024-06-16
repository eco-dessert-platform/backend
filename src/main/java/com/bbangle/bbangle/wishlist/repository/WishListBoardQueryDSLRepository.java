package com.bbangle.bbangle.wishlist.repository;

import com.bbangle.bbangle.analytics.dto.DateAndCountDto;

import com.bbangle.bbangle.config.ranking.BoardWishCount;
import java.time.LocalDate;
import java.util.List;

public interface WishListBoardQueryDSLRepository {

    List<DateAndCountDto> countWishlistCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate);

    List<BoardWishCount> groupByBoardIdAndGetWishCount();

}
