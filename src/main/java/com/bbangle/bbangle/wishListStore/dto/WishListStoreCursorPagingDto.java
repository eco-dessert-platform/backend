package com.bbangle.bbangle.wishListStore.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class WishListStoreCursorPagingDto {
   private static final long LAST_PAGE = -1L;
   private List<WishListStoreResponseDto> contents;
   private long nextCursor;
   private int dataCntPerPage;


   public static WishListStoreCursorPagingDto of(
           List<WishListStoreResponseDto> wishListStoreResponseDtos,
           int size
   ) {
      if(isLastPage(wishListStoreResponseDtos, size)){
         return new WishListStoreCursorPagingDto(wishListStoreResponseDtos, LAST_PAGE, size);
      }
      return WishListStoreCursorPagingDto.hasNextScroll(wishListStoreResponseDtos, size);
   }

   private static boolean isLastPage(List<WishListStoreResponseDto> wishListStoreResponseDtos, int size) {
      return wishListStoreResponseDtos.size() < size;
   }

   private static WishListStoreCursorPagingDto hasNextScroll(
           List<WishListStoreResponseDto> wishListStoreResponseDtos,
           int size
   ) {
      int infoSize = wishListStoreResponseDtos.size();
      WishListStoreResponseDto currentLastPageInfo = wishListStoreResponseDtos.get(infoSize - 1);
      Long currentLastId = currentLastPageInfo.getId();
      return new WishListStoreCursorPagingDto(wishListStoreResponseDtos, currentLastId, size);
   }
}
