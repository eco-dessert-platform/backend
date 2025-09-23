package com.bbangle.bbangle.seller.store.board.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.store.board.controller.dto.ProductBoardUpdateRequest;
import com.bbangle.bbangle.seller.store.board.controller.swagger.ProductBoardApi;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/seller/store")
@Slf4j
public class ProductBoardController implements ProductBoardApi {

    private final ResponseService responseService;

    @Override
    @PutMapping("{storeId}/boards/{boardId}")
    public CommonResult changeProductBoard(
        @PathVariable(name = "storeId") Long storeId,
        @PathVariable(name = "boardId") Long boardId,
        @Valid @RequestBody ProductBoardUpdateRequest request) {
        // TODO: 비즈니스 로직 구현 예정
        return responseService.getSingleResult(request);
    }
}
