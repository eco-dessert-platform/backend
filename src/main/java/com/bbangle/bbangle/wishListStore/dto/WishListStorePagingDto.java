package com.bbangle.bbangle.wishListStore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@AllArgsConstructor
@Getter
public class WishListStorePagingDto {
    private static final long LAST_PAGE = -1L;
    private List<WishListStoreResponseDto> contents;
    private long lastPage;
    private long nextPage;

    public static WishListStorePagingDto of(Page<WishListStoreResponseDto> wishStorePaging){
        if(!wishStorePaging.hasNext()){
            return WishListStorePagingDto.getLastPage(wishStorePaging.getContent(), wishStorePaging.getTotalPages()-1);
        }
        return WishListStorePagingDto.newPagingHasNext(wishStorePaging.getContent(),
                wishStorePaging.getTotalPages()-1,
                wishStorePaging.getPageable().getPageNumber()+1);
    }
    private static WishListStorePagingDto getLastPage(List<WishListStoreResponseDto> wishStorePaging, long lastPage) {
        return newPagingHasNext(wishStorePaging, lastPage, LAST_PAGE);
    }

    private static WishListStorePagingDto newPagingHasNext(List<WishListStoreResponseDto> wishStorePaging, long lastPage, long nextPage) {
        return new WishListStorePagingDto(wishStorePaging, lastPage, nextPage);
    }
}
