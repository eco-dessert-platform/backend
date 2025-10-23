package com.bbangle.bbangle.board.seller.controller;

import com.bbangle.bbangle.board.seller.controller.dto.request.BoardUploadRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.ProductBoardRequest.ProductBoardSearchRequest;
import com.bbangle.bbangle.board.seller.controller.dto.request.ProductBoardUpdateRequest;
import com.bbangle.bbangle.board.seller.controller.dto.response.SellerBoardResponse.SellerBoardSearchResponse;
import com.bbangle.bbangle.board.seller.controller.swagger.SellerBoardApi;
import com.bbangle.bbangle.board.seller.service.BoardUploadService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/seller/boards")
public class SellerBoardController implements SellerBoardApi {

    private final ResponseService responseService;
    private final BoardUploadService boardUploadService;

    @PostMapping("/board/{storeId}")
    @Override
    public CommonResult upload(
        @PathVariable Long storeId,
        @RequestBody BoardUploadRequest request) {
        boardUploadService.upload(storeId, request);
        return responseService.getSuccessResult();
    }

    @Override
    @GetMapping("/{storeId}/boards")
    public SingleResult<BbanglePageResponse<SellerBoardSearchResponse>> searchProductBoard(
        @Valid
        @PathVariable(name = "storeId")
        Long storeId,
        ProductBoardSearchRequest request) {

        // TODO : 비즈니스 로직 차후 구현 예정

        PageRequest pageable = PageRequest.of(request.page(), request.size());
        Page<SellerBoardSearchResponse> resultPage = new PageImpl<>(new ArrayList<>(), pageable,
            0);

        BbanglePageResponse<SellerBoardSearchResponse> res = BbanglePageResponse.of(resultPage);
        return responseService.getSingleResult(res);
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
