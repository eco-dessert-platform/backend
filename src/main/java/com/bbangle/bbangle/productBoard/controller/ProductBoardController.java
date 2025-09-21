package com.bbangle.bbangle.productBoard.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.page.PaginatedResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.productBoard.controller.ProductBoardRequest.ProductBoardSearchRequest;
import com.bbangle.bbangle.productBoard.controller.ProductBoardResponse.ProductBoardSearchResponse;
import com.bbangle.bbangle.productBoard.controller.swagger.ProductBoardApi;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductBoardController implements ProductBoardApi {

    private final ResponseService responseService;

    @Override
    public PaginatedResponse<ProductBoardSearchResponse> searchProductBoard(
        Long storeId,
        ProductBoardSearchRequest request) {
        // TODO : 비즈니스 로직 차후 구현 예정

        PageRequest pageable = PageRequest.of(request.page(), request.size());
        Page<ProductBoardSearchResponse> resultPage = new PageImpl<>(new ArrayList<>(), pageable,
            0);

        PaginatedResponse<ProductBoardSearchResponse> response = new PaginatedResponse<>();
        response.setContent(resultPage.getContent());
        response.setPageNumber(resultPage.getNumber());
        response.setPageSize(resultPage.getSize());
        response.setTotalPages(resultPage.getTotalPages());
        response.setTotalElements(resultPage.getTotalElements());

        return responseService.getPagingResult(response);
    }

    @Override
    public CommonResult removeProductBoards(Long storeId, List<Long> boardIds) {
        // TODO: 비즈니스 로직 구현 예정
        return responseService.getSuccessResult();
    }

    @Override
    public CommonResult changeProductBoard(Long storeId, Long productBoardId,
        ProductBoardRequest.ProductBoardUpdateRequest request) {
        // TODO: 비즈니스 로직 구현 예정
        return responseService.getSingleResult(request);
    }

    @Override
    public CommonResult copyProductBoard(Long storeId, Long productBoardId) {
        return responseService.getSuccessResult();
    }
}
