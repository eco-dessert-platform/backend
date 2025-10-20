package com.bbangle.bbangle.seller.store.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.page.PaginatedResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.store.controller.dto.ProductBoardRequest.ProductBoardSearchRequest;
import com.bbangle.bbangle.seller.store.controller.dto.ProductBoardResponse.ProductBoardSearchResponse;
import com.bbangle.bbangle.seller.store.controller.dto.ProductBoardUpdateRequest;
import com.bbangle.bbangle.seller.store.controller.swagger.ProductBoardApi;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductBoardController implements ProductBoardApi {

    private final ResponseService responseService;


    @Override
    @GetMapping("/{storeId}/boards")
    public PaginatedResponse<ProductBoardSearchResponse> searchProductBoard(
        @Valid
        @PathVariable(name = "storeId")
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
    @PutMapping("{storeId}/boards/{boardId}")
    public CommonResult changeProductBoard(
        @PathVariable(name = "storeId") Long storeId,
        @PathVariable(name = "boardId") Long boardId,
        @Valid @RequestBody ProductBoardUpdateRequest request) {
        // TODO: 비즈니스 로직 구현 예정
        return responseService.getSingleResult(request);
    }

    @Override
    @PostMapping("{storeId}/boards/{boardId}")
    public CommonResult copyProductBoard(
        @PathVariable(name = "storeId") Long storeId,
        @PathVariable(name = "boardId") Long boardId) {
        return responseService.getSuccessResult();
    }


    @Override
    @PostMapping("/{storeId}/boards")
    public CommonResult removeProductBoards(
        @PathVariable(name = "storeId") Long storeId,
        @RequestBody List<Long> boardIds) {
        // TODO: 비즈니스 로직 구현 예정
        return responseService.getSuccessResult();
    }
}
