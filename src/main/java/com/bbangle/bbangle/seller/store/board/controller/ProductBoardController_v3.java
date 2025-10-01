package com.bbangle.bbangle.seller.store.board.controller;

import com.bbangle.bbangle.common.page.PaginatedResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.seller.store.board.controller.dto.ProductBoardRequest_v3;
import com.bbangle.bbangle.seller.store.board.controller.dto.ProductBoardResponse_v3.ProductBoardSearchResponse;
import com.bbangle.bbangle.seller.store.board.controller.swagger.ProductBoardApi_v3;
import jakarta.validation.Valid;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/seller/store")
@Slf4j
public class ProductBoardController_v3 implements ProductBoardApi_v3 {

    private final ResponseService responseService;

    @Override
    @GetMapping("/{storeId}/boards")
    public PaginatedResponse<ProductBoardSearchResponse> searchProductBoard(
        @Valid
        @PathVariable(name = "storeId")
        Long storeId,
        ProductBoardRequest_v3.ProductBoardSearchRequest request) {

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
}
