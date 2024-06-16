package com.bbangle.bbangle.store.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.util.Objects;
import lombok.Getter;

@Getter
public class StoreDetailStoreDto {

    private Long storeId;
    private String profile;
    private String storeName;
    private String introduce;
    private Boolean isWished;

    @QueryProjection
    public StoreDetailStoreDto(
        Long storeId,
        String profile,
        String storeName,
        String introduce,
        Long isWished
    ) {
        this.storeId = storeId;
        this.profile = profile;
        this.storeName = storeName;
        this.introduce = introduce;
        this.isWished = isNonEmptyWishlist(isWished);
    }

    private Boolean isNonEmptyWishlist(Long wishlistId) {
        return Objects.nonNull(wishlistId) && wishlistId > 0;
    }
}