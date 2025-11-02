package com.bbangle.bbangle.wishlist.customer.dto;

import com.bbangle.bbangle.common.page.CustomPage;
import java.util.List;

public class WishListStoreCustomPage<T> extends CustomPage {

    public WishListStoreCustomPage(T content, Long requestCursor, Boolean hasNext) {
        super(content, requestCursor, hasNext);
    }

    public static WishListStoreCustomPage<List<WishListStoreResponseDto>> from(
        List<WishListStoreResponseDto> wishListStoreResponseDtoList,
        Long requestCursor,
        Boolean hasNext
    ) {
        return new WishListStoreCustomPage<>(wishListStoreResponseDtoList, requestCursor, hasNext);
    }


}
