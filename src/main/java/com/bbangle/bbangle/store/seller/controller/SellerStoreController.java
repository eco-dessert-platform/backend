package com.bbangle.bbangle.store.seller.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.seller.controller.swagger.SellerStoreApi;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// TODO : Test
@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/stores")
@Validated
public class SellerStoreController implements SellerStoreApi {

    private final ResponseService responseService;
    private final SellerStoreService sellerStoreService;

    @Override
    @GetMapping("/search")
    public SingleResult<CursorPagination<SellerStoreInfo.StoreInfo>> search(
        @RequestParam @NotBlank(message = "스토어 이름은 필수입니다.") String storeName,
        @RequestParam(required = false) Long cursorId
    ) {
        return responseService.getSingleResult(
            sellerStoreService.selectStoreNameForSeller(storeName, cursorId)
        );
    }

    @Override
    @GetMapping("/check-name")
    public SingleResult<StoreResponse.StoreNameCheck> checkStoreNameDuplicate(
        @RequestParam @NotBlank(message = "스토어 이름은 필수입니다.") String storeName
    ) {
        return responseService.getSingleResult(sellerStoreService.checkStoreName(storeName));
    }
}
