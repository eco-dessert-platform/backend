package com.bbangle.bbangle.wishlist.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;

@Getter
public class WishListStoreResponseDto {
    private String introduce;
    private String storeName;
    private Long storeId;
    private Boolean isWished;
    private String profile;

    @Builder
    @QueryProjection
    public WishListStoreResponseDto(String introduce,
                                    String storeName,
                                    Long storeId,
                                    String profile) {
        this.introduce = introduce;
        this.storeName = storeName;
        this.storeId = storeId;
        this.isWished = true;
        this.profile = profile;
    }
}
