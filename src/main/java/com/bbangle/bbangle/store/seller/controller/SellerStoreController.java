package com.bbangle.bbangle.store.seller.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.swagger.SellerStoreApi;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// TODO : Test
@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/stores")
public class SellerStoreController implements SellerStoreApi {

    private final ResponseService responseService;
    private final SellerStoreService sellerStoreService;

    @Override
    @GetMapping("/search")
    public SingleResult<CursorPagination<SellerStoreInfo.StoreInfo>> search(
        @RequestParam String storeName
    ) {
        return responseService.getSingleResult(
            sellerStoreService.selectStoreNameForSeller(storeName)
        );
    }


    @Override
    @GetMapping("/check-name")
    public SingleResult<StoreResponse.StoreNameCheck> checkStoreNameDuplicate(
        @RequestParam String storeName
    ) {
        return responseService.getSingleResult(sellerStoreService.checkStoreName(storeName));
    }
}
