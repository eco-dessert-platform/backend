package com.bbangle.bbangle.wishListStore.repository;

import com.bbangle.bbangle.wishListStore.dto.WishListStoreResponseDto;
import com.bbangle.bbangle.wishListStore.domain.WishlistStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface WishListStoreQueryDSLRepository {
    Page<WishListStoreResponseDto> getWishListStoreResponse(Long memberId, Pageable pageable);
    Optional<WishlistStore> findWishListStore(Long memberId, Long storeId);
    List<WishlistStore> findWishListStores(Long memberId);

}
