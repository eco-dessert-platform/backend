package com.bbangle.bbangle.productBoard.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.productBoard.controller.swagger.ProductBoardApi;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductBoardController implements ProductBoardApi {

    private final ResponseService responseService;

    @Override
    public CommonResult removeProductBoards(Long storeId, List<Long> boardIds) {
        // TODO: 비즈니스 로직 구현 예정
        return responseService.getSuccessResult();
    }
}
