package com.bbangle.bbangle.store.board.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.store.board.controller.swagger.ProductBoardApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/seller/store/")
@Slf4j
public class ProductBoardController implements ProductBoardApi {

    private final ResponseService responseService;


    @Override
    @PostMapping("{storeId}/boards/{boardId}")
    public CommonResult copyProductBoard(
        @PathVariable(name = "storeId") Long storeId,
        @PathVariable(name = "boardId") Long boardId) {
        return responseService.getSuccessResult();
    }
}
