package com.bbangle.bbangle.wishListStore.service;

import com.bbangle.bbangle.wishListStore.dto.WishListStoreCursorPagingDto;
import com.bbangle.bbangle.wishListStore.dto.WishListStorePagingDto;
import org.springframework.data.domain.Pageable;

public interface WishListStoreService {
    WishListStorePagingDto getWishListStoresRes(Long memberId, Pageable pageable);

    void deletedByDeletedMember(Long memberId);

    WishListStoreCursorPagingDto getWishListStoresResByCursor(Long memberId, Long cursorId, int size);
}
