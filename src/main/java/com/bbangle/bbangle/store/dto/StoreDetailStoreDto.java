package com.bbangle.bbangle.store.dto;

import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListStore;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoreDetailStoreDto {

    private Long storeId;
    private String profile;
    private String storeName;
    private String introduce;
    private Boolean isWished;

    public static StoreDetailStoreDto create(
        Store store,
        WishListStore wishListStore
    ) {
        return new StoreDetailStoreDto(
            store.getId(),
            store.getProfile(),
            store.getName(),
            store.getIntroduce(),
            isNonEmptyWishlist(wishListStore.getId())
        );
    }

    private static Boolean isNonEmptyWishlist(Long wishlistId) {
        return Objects.nonNull(wishlistId) && wishlistId > 0;
    }
}