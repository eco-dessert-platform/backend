package com.bbangle.bbangle.wishListStore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class WishListStorePagingDto {
    private static final long LAST_PAGE = -1L;

    private List<WishListStoreResponseDto> contents;
    private long lastPage;
    private long nextPage;

    /**
     * 위시리스트 스토어 페이지네이션 응답 dto 생성
     *
     * @param wishStorePaging 위시리스트 스토어 정보를 담고 있는 객체
     * @return 위시리스트 스토어 페이지네이션 응답 dto
     */
    public static WishListStorePagingDto of(Page<WishListStoreResponseDto> wishStorePaging){
        //마지막 페이지를 요청했다면
        if(!wishStorePaging.hasNext()){
            return WishListStorePagingDto.newLastScroll(wishStorePaging.getContent(), wishStorePaging.getTotalPages()-1);
        }
        return WishListStorePagingDto.newPagingHasNext(wishStorePaging.getContent(),
                wishStorePaging.getTotalPages()-1, //page는 0부터 시작
                wishStorePaging.getPageable().getPageNumber()+1);
    }
    //마지막 페이지인 경우
    private static WishListStorePagingDto newLastScroll(List<WishListStoreResponseDto> wishStorePaging, long lastPage) {
        return newPagingHasNext(wishStorePaging, lastPage, LAST_PAGE);
    }

    //마지막 페이지가 아닌 경우
    private static WishListStorePagingDto newPagingHasNext(List<WishListStoreResponseDto> wishStorePaging, long lastPage, long nextPage) {
        return new WishListStorePagingDto(wishStorePaging, lastPage, nextPage);
    }
}
