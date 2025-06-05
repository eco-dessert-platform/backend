package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.controller.mapper.BoardDetailMapper;
import com.bbangle.bbangle.board.dto.BoardDetailResponse.Main;
import com.bbangle.bbangle.board.dto.BoardImageDetailResponse;
import com.bbangle.bbangle.board.dto.SimilarityBoardResponse;
import com.bbangle.bbangle.board.dto.orders.ProductResponse;
import com.bbangle.bbangle.board.facade.BoardDetailFacade;
import com.bbangle.bbangle.board.service.BoardDetailService;
import com.bbangle.bbangle.board.service.ProductService;
import com.bbangle.bbangle.board.service.dto.StoreInfo.Store;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.review.dto.SummarizedReviewResponse;
import com.bbangle.bbangle.review.service.ReviewService;
import com.bbangle.bbangle.board.facade.StoreFacade;
import com.bbangle.bbangle.board.service.StoreService;
import com.bbangle.bbangle.board.service.dto.StoreInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private final StoreFacade storeFacade;

    @Operation(summary = "게시판 조회")
    @GetMapping("/{boardId}")
    public SingleResult<BoardImageDetailResponse> getBoardDetailResponse(
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

    // 프론트에서 /new API 변경 후 삭제 예정
    @Operation(summary = "스토어 조회")
    @GetMapping("/{boardId}/store")
    public SingleResult<Store> getStoreInfoInBoardDetail(
            @PathVariable("boardId")
            Long boardId,
            @AuthenticationPrincipal
            Long memberId
    ) {
        StoreInfo.Store storeInfo = storeFacade.getStoreInfo(memberId, boardId);
        return responseService.getSingleResult(storeInfo);
    }

    @GetMapping("/{boardId}/similar_board")
    public SingleResult<List<SimilarityBoardResponse>> getCount(
            @PathVariable(value = "boardId")
            Long boardId,
            @AuthenticationPrincipal
            Long memberId
    ) {
        List<SimilarityBoardResponse> similarityBoardResponses = boardDetailService.getSimilarityBoardResponses(
                memberId, boardId);
        return responseService.getSingleResult(similarityBoardResponses);
    }

    // 프론트에서 /new API 변경 후 삭제 예정
    @Operation(summary = "상품 조회")
    @GetMapping("/{boardId}/product")
    public SingleResult<ProductResponse> getProductResponse(
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
    public SingleResult<SummarizedReviewResponse> getReviewResponse(
            @PathVariable("boardId")
            Long boardId) {
        SummarizedReviewResponse response = reviewService.getSummarizedReview(boardId);

        return responseService.getSingleResult(response);
    }

    @Operation(summary = "게시판 조회(new)")
    @GetMapping("/{boardId}/new")
    public SingleResult<Main> getBoardDetail(
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

