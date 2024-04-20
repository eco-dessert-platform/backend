package com.bbangle.bbangle.wishListStore.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.util.SecurityUtils;
import com.bbangle.bbangle.wishListStore.dto.WishListStorePagingDto;
import com.bbangle.bbangle.wishListStore.service.WishListStoreServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/likes")
public class WishListStoreController {
    private final WishListStoreServiceImpl wishlistStoreService;
    private final ResponseService responseService;

    @GetMapping("/stores")
    public CommonResult getWishListStores(Pageable pageable){
        Long memberId = SecurityUtils.getMemberId();
        WishListStorePagingDto wishListStoresRes = wishlistStoreService.getWishListStoresResponse(memberId, pageable);
        return responseService.getSingleResult(wishListStoresRes);
    }

    @PostMapping("/store/{storeId}")
    public CommonResult addWishListStore(@PathVariable Long storeId){
        Long memberId = SecurityUtils.getMemberId();
        try {
            wishlistStoreService.save(memberId, storeId);
            return responseService.getSuccessResult("스토어를 찜했습니다", 0);
        }catch (Exception e){
            return responseService.getFailResult("스토어를 찜하지 못했습니다", -1);
        }
    }

    @PatchMapping("/store/{storeId}")
    public CommonResult deleteWishListStore(@PathVariable Long storeId){
        Long memberId = SecurityUtils.getMemberId();
         wishlistStoreService.deleteStore(memberId, storeId);
         return responseService.getSuccessResult("스토어 찜을 해제했습니다", 0);
    }
}
