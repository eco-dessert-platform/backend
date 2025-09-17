package com.bbangle.bbangle.productBoard.controller;

import com.bbangle.bbangle.common.page.PaginatedResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.productBoard.controller.ProductBoardRequest.ProductBoardSearchRequest;
import com.bbangle.bbangle.productBoard.controller.ProductBoardResponse.ProductBoardSearchResponse;
import com.bbangle.bbangle.productBoard.controller.swagger.ProductBoardApi;
import java.util.ArrayList;
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
        ProductBoardSearchRequest request) {
        // TODO : 비즈니스 로직 차후 구현 예정
        // In a real implementation, you would call your service to get a Page object.
        // e.g., Page<ProductBoardSearchResponse> resultPage = productBoardService.search(request);

        // For demonstration, we'll create a mock Page object here:
        PageRequest pageable = PageRequest.of(request.page(), request.size());
        Page<ProductBoardSearchResponse> resultPage = new PageImpl<>(new ArrayList<>(), pageable,
            0);

        // Create and populate the PaginatedResponse
        PaginatedResponse<ProductBoardSearchResponse> response = new PaginatedResponse<>();
        response.setContent(resultPage.getContent());
        response.setPageNumber(resultPage.getNumber());
        response.setPageSize(resultPage.getSize());
        response.setTotalPages(resultPage.getTotalPages());
        response.setTotalElements(resultPage.getTotalElements());

        // Set success status
        response.setSuccess(true);
        response.setCode(0);
        response.setMessage("상품 게시글 조회 성공");

        log.info("info {}", response);

        return response;
    }
}
