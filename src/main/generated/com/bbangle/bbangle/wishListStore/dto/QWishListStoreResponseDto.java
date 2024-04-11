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

    public QWishListStoreResponseDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> introduce, com.querydsl.core.types.Expression<String> storeName, com.querydsl.core.types.Expression<Long> storeId, com.querydsl.core.types.Expression<String> profile) {
        super(WishListStoreResponseDto.class, new Class<?>[]{long.class, String.class, String.class, long.class, String.class}, id, introduce, storeName, storeId, profile);
    }

}

