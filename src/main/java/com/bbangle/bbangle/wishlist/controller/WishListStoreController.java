package com.bbangle.bbangle.wishlist.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.MessageDto;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.util.SecurityUtils;
import com.bbangle.bbangle.wishlist.service.WishListStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/likes")
public class WishListStoreController {

    private final WishListStoreService wishlistStoreService;
    private final ResponseService responseService;
    private static final String SAVE_WISHLIST_STORE = "스토어를 찜했습니다";
    private static final String REMOVE_WISHLIST_STORE = "스토어 찜을 해제했습니다";

    @GetMapping("/stores")
    public CommonResult getWishListStores(
        @RequestParam(required = false, value = "cursorId") Long cursorId) {
        Long memberId = SecurityUtils.getMemberId();
        return responseService.getSingleResult(
            wishlistStoreService.getWishListStoresResponse(memberId, cursorId));
    }

    @PostMapping("/store/{storeId}")
    public CommonResult addWishListStore(@PathVariable("storeId") Long storeId) {
        Long memberId = SecurityUtils.getMemberId();
        wishlistStoreService.save(memberId, storeId);
        return responseService.getSingleResult(MessageDto.builder()
            .message(SAVE_WISHLIST_STORE)
            .build());
    }

    @PatchMapping("/store/{storeId}")
    public CommonResult deleteWishListStore(@PathVariable("storeId") Long storeId) {
        Long memberId = SecurityUtils.getMemberId();
        wishlistStoreService.deleteStore(memberId, storeId);
        return responseService.getSingleResult(MessageDto.builder()
            .message(REMOVE_WISHLIST_STORE)
            .build());
    }
}
