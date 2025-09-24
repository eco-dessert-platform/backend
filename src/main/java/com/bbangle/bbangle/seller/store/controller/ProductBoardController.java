package com.bbangle.bbangle.seller.store.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.store.controller.swagger.ProductBoardApi;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    @PostMapping("/{storeId}/boards")
    public CommonResult removeProductBoards(
        @PathVariable(name = "storeId") Long storeId,
        @RequestBody List<Long> boardIds) {
        // TODO: 비즈니스 로직 구현 예정
        return responseService.getSuccessResult();
    }
}
