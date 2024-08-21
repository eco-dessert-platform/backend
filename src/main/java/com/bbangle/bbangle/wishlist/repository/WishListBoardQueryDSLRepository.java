package com.bbangle.bbangle.wishlist.repository;

import com.bbangle.bbangle.analytics.dto.DateAndCountDto;

import com.bbangle.bbangle.boardstatistic.ranking.BoardWishCount;
import com.bbangle.bbangle.wishlist.dao.WishListStatisticDao;
import java.time.LocalDate;
import java.util.List;

public interface WishListBoardQueryDSLRepository {

    List<DateAndCountDto> countWishlistCreatedBetweenPeriod(LocalDate startLocalDate, LocalDate endLocalDate);

    List<BoardWishCount> groupByBoardIdAndGetWishCount();

    List<WishListStatisticDao> findWishStatisticByBoardIds(List<Long> boardWishUpdateId);

}
