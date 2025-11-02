package com.bbangle.bbangle.wishlist.repository.dao;

import com.querydsl.core.annotations.QueryProjection;

public record WishListStatisticDao(
    Long boardId,
    Long wishListCount
) {

    @QueryProjection
    public WishListStatisticDao {

    }

}
