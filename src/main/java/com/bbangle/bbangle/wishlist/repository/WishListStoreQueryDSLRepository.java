package com.bbangle.bbangle.wishlist.repository;

import com.bbangle.bbangle.wishlist.domain.WishListStore;
import com.bbangle.bbangle.wishlist.dto.WishListStoreCustomPage;
import com.bbangle.bbangle.wishlist.dto.WishListStoreResponseDto;
import com.bbangle.bbangle.wishlist.domain.WishListStore;

import java.util.List;
import java.util.Optional;

public interface WishListStoreQueryDSLRepository {
    WishListStoreCustomPage<List<WishListStoreResponseDto>> getWishListStoreResponse(Long memberId, Long cursorId);
    Optional<WishListStore> findWishListStore(Long memberId, Long storeId);
    List<WishListStore> findWishListStores(Long memberId);

}
