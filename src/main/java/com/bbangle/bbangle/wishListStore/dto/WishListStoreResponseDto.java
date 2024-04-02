package com.bbangle.bbangle.wishListStore.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;


@Getter
public class WishListStoreResponseDto {
    private Long id;
    private String introduce;
    private String storeName;
    private Long storeId;
    private boolean isWished;
    private String profile;

    @QueryProjection
    @Builder
    public WishListStoreResponseDto(Long id,
                                    String introduce,
                                    String storeName,
                                    Long storeId,
                                    String profile) {
        this.id = id;
        this.introduce = introduce;
        this.storeName = storeName;
        this.storeId = storeId;
        this.isWished = true;
        this.profile = profile;
    }
}
