package com.bbangle.bbangle.store.seller.controller;

import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.SearchResponse;
import com.bbangle.bbangle.store.seller.controller.swagger.SellerStoreApi;
import com.bbangle.bbangle.store.seller.service.SellerStoreService;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/stores")
public class SellerStoreController implements SellerStoreApi {

    private final ResponseService responseService;
    private final SellerStoreService sellerStoreService;

    @Override
    @GetMapping("/search")
    public ListResult<SearchResponse> search(@RequestParam String searchValue) {
        // TODO: 구현 필요
        List<SearchResponse> response = new ArrayList<>();
        response.add(new SearchResponse(1L, "빵그리의 오븐 즉석빵 상점"));
        response.add(new SearchResponse(2L, "빵그리의 오븐 공장빵 상점"));
        return responseService.getListResult(response);
    }


    @Override
    @GetMapping("/check-name-duplicate")
    public SingleResult<CursorPagination<SellerStoreInfo.StoreInfo>> checkStoreNameDuplicate(
        @RequestParam String storeName) {

        CursorPagination<SellerStoreInfo.StoreInfo> result = sellerStoreService.selectStoreNameForSeller(
            storeName);

        return responseService.getSingleResult(result);
    }
}
