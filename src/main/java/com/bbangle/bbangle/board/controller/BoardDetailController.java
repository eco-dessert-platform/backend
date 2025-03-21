package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.controller.mapper.BoardDetailMapper;
import com.bbangle.bbangle.board.dto.BoardImageDetailResponse;
import com.bbangle.bbangle.board.dto.SimilarityBoardResponse;
import com.bbangle.bbangle.board.dto.orders.ProductResponse;
import com.bbangle.bbangle.board.facade.BoardDetailFacade;
import com.bbangle.bbangle.board.service.BoardDetailService;
import com.bbangle.bbangle.board.service.ProductService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.review.dto.SummarizedReviewResponse;
import com.bbangle.bbangle.review.service.ReviewService;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/boards")
@Tag(name = "BoardDetail", description = "게시판 상세정보 API")
@RequiredArgsConstructor
public class BoardDetailController {

    private final StoreService storeService;
    private final BoardDetailService boardDetailService;
    private final ResponseService responseService;
    private final ProductService productService;
    private final ReviewService reviewService;
    private final BoardDetailMapper boardDetailMapper;
    private final BoardDetailFacade boardDetailFacade;

    @Operation(summary = "게시판 조회")
    @GetMapping("/{boardId}")
    public CommonResult getBoardDetailResponse(
            @PathVariable("boardId")
            Long boardId,
            @AuthenticationPrincipal
            Long memberId,
            HttpServletRequest httpServletRequest
    ) {
        BoardImageDetailResponse response = boardDetailService.getBoardDtos(memberId, boardId,
                httpServletRequest.getRemoteAddr());
        return responseService.getSingleResult(response);
    }

    @Operation(summary = "스토어 조회")
    @GetMapping("/{boardId}/store")
    public CommonResult getStoreInfoInBoardDetail(
            @PathVariable("boardId")
            Long boardId,
            @AuthenticationPrincipal
            Long memberId
    ) {
        StoreDto storeDto = storeService.getStoreDtoByBoardId(memberId, boardId);
        return responseService.getSingleResult(storeDto);
    }

    @GetMapping("/{boardId}/similar_board")
    public CommonResult getCount(
            @PathVariable(value = "boardId")
            Long boardId,
            @AuthenticationPrincipal
            Long memberId
    ) {
        List<SimilarityBoardResponse> similarityBoardResponses = boardDetailService.getSimilarityBoardResponses(
                memberId, boardId);
        return responseService.getSingleResult(similarityBoardResponses);
    }

    @Operation(summary = "상품 조회")
    @GetMapping("/{boardId}/product")
    public CommonResult getProductResponse(
            @PathVariable("boardId")
            Long boardId,
            @AuthenticationPrincipal
            Long memberId
    ) {
        if (Objects.nonNull(memberId)) {
            ProductResponse productResponse = productService.getProductResponseWithPush(memberId,
                    boardId);
            return responseService.getSingleResult(productResponse);
        }

        ProductResponse productResponse = productService.getProductResponse(boardId);
        return responseService.getSingleResult(productResponse);
    }

    @Operation(summary = "리뷰 조회")
    @GetMapping("/{boardId}/review")
    public CommonResult getReviewResponse(
            @PathVariable("boardId")
            Long boardId) {
        SummarizedReviewResponse response = reviewService.getSummarizedReview(boardId);

        return responseService.getSingleResult(response);
    }

    @Operation(summary = "게시판 조회(new)")
    @GetMapping("/{boardId}/new")
    public CommonResult getBoardDetail(
            @PathVariable("boardId")
            Long boardId,
            @AuthenticationPrincipal
            Long memberId,
            HttpServletRequest httpServletRequest
    ) {
        var command = boardDetailMapper.toBoardDetailMain(boardId, memberId, httpServletRequest);
        var info = boardDetailFacade.getBoardDetail(command);
        var response = boardDetailMapper.toBoardDetailMainResponse(info);
        return responseService.getSingleResult(response);
    }

}

