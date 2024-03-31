package com.bbangle.bbangle.wishListStore.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.bbangle.bbangle.wishListStore.dto.QWishListStoreResponseDto is a Querydsl Projection type for WishListStoreResponseDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QWishListStoreResponseDto extends ConstructorExpression<WishListStoreResponseDto> {

    private static final long serialVersionUID = -501167034L;

    public QWishListStoreResponseDto(com.querydsl.core.types.Expression<String> introduce, com.querydsl.core.types.Expression<String> storeName, com.querydsl.core.types.Expression<Long> storeId) {
        super(WishListStoreResponseDto.class, new Class<?>[]{String.class, String.class, long.class}, introduce, storeName, storeId);
    }

}

