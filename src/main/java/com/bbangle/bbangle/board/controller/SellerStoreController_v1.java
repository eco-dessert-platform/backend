package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.controller.dto.StoreResponse.SearchResponse;
import com.bbangle.bbangle.board.controller.swagger.SellerStoreApi_v1;
import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.service.ResponseService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/seller/stores")
public class SellerStoreController_v1 implements SellerStoreApi_v1 {

    private final ResponseService responseService;

    @Override
    @GetMapping("/search")
    public ListResult<SearchResponse> search(@RequestParam String searchValue) {
        // TODO: 구현 필요
        List<SearchResponse> response = new ArrayList<>();
        response.add(new SearchResponse(1L, "빵그리의 오븐 즉석빵 상점"));
        response.add(new SearchResponse(2L, "빵그리의 오븐 공장빵 상점"));
        return responseService.getListResult(response);
    }

}
