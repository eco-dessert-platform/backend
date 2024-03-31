package com.bbangle.bbangle.wishListStore.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class WishListStoreResponseDto {
    private Long id;
    private String introduce;
    private String storeName;
    private Long storeId;

    @QueryProjection
    public WishListStoreResponseDto(Long id, String introduce, String storeName, Long storeId) {
        this.id = id;
        this.introduce = introduce;
        this.storeName = storeName;
        this.storeId = storeId;
    }
}
