package com.bbangle.bbangle.wishListStore.controller;

import com.bbangle.bbangle.common.message.MessageResDto;
import com.bbangle.bbangle.wishListStore.dto.WishListStoreCursorPagingDto;
import com.bbangle.bbangle.wishListStore.dto.WishListStorePagingDto;
import com.bbangle.bbangle.wishListStore.service.WishListStoreServiceImpl;
import com.bbangle.bbangle.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/likes")
public class WishListStoreController {
    private final WishListStoreServiceImpl wishlistStoreService;
    private static final Long MAX_CURSOR = 9999999L;

    /**
     * 스토어 위시리스트 조회 (오프셋 기반 페이지 네이션)
     *
     * @return the response entity List<wishListStoreResDto>
     */
    @GetMapping("/stores")
    public ResponseEntity<WishListStorePagingDto> getWishListStores(Pageable pageable){
        long before = System.currentTimeMillis();
        Long memberId = SecurityUtils.getMemberId();
        WishListStorePagingDto wishListStoresRes = wishlistStoreService.getWishListStoresRes(memberId, pageable);
        long after = System.currentTimeMillis();
        log.info("take Time : {}", after-before);
        return ResponseEntity.ok().body(wishListStoresRes);
    }

    /**
     * 스토어 위시리스트 조회 (커서 기반 페이지 네이션)
     *
     * @param cursorId the cursor id
     * @return the wish list stores by cursor
     */
    @GetMapping("/stores/cursor")
    public ResponseEntity<WishListStoreCursorPagingDto> getWishListStoresByCursor(
            @RequestParam(required = false) Long cursorId,
            @RequestParam int size
    ){
        long before = System.currentTimeMillis();
        Long memberId = SecurityUtils.getMemberId();
        if(cursorId == null){
            cursorId = MAX_CURSOR;
        }
        WishListStoreCursorPagingDto wishListStoreRes =
        wishlistStoreService.getWishListStoresResByCursor(memberId, cursorId, size);
        long after = System.currentTimeMillis();
        log.info("take Time : {}", after-before);
        return ResponseEntity.ok().body(wishListStoreRes);
    }

    /**
     * 스토어 위시리스트 추가
     *
     * @param storeId 스토어 id
     * @hidden memberId 멤버 id
     * @return 메세지
     */
    @PostMapping("/store/{storeId}")
    public ResponseEntity<MessageResDto> addWishListStore(@PathVariable Long storeId){
        Long memberId = SecurityUtils.getMemberId();
        try {
            wishlistStoreService.save(memberId, storeId);
            return ResponseEntity.ok().body(new MessageResDto("스토어를 찜했습니다"));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResDto("스토어를 찜하지 못했습니다"));
        }
    }

    /**
     * 스토어 위시리스트 삭제
     *
     * @param storeId the store id
     * @return the response entity
     */
    @PatchMapping("/store/{storeId}")
    public ResponseEntity<MessageResDto> deleteWishListStore(@PathVariable Long storeId){
        Long memberId = SecurityUtils.getMemberId();
         wishlistStoreService.deleteStore(memberId, storeId);
         return ResponseEntity.ok().body(new MessageResDto("스토어 찜을 해제했습니다"));
    }
}
