package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.dto.BoardImageDetailResponse;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.CursorInfo;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.dto.ProductResponse;
import com.bbangle.bbangle.board.service.BoardService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.common.sort.SortType;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.page.CustomPage;
import com.bbangle.bbangle.review.dto.SummarizedReviewResponse;
import com.bbangle.bbangle.review.service.ReviewService;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.service.StoreService;
import com.bbangle.bbangle.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/boards")
@Tag(name = "Boards", description = "게시판 API")
@RequiredArgsConstructor
public class BoardController {

    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final ResponseService responseService;
    private final BoardService boardService;
    private final StoreService storeService;
    private final ReviewService reviewService;

    @Operation(summary = "게시글 전체 조회")
    @ApiResponse(
        responseCode = "200",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CustomPage.class)
        )
    )
    @GetMapping
    public CommonResult getList(
        @ParameterObject
        FilterRequest filterRequest,
        @RequestParam(required = false, value = "sort")
        SortType sort,
        @ParameterObject
        CursorInfo cursorInfo,
        @AuthenticationPrincipal
        Long memberId
    ) {
        BoardCustomPage<List<BoardResponseDto>> boardResponseList = boardService.getBoardList(
            filterRequest,
            sort,
            cursorInfo,
            memberId);
        return responseService.getSingleResult(boardResponseList);
    }

    @GetMapping("/folders/{folderId}")
    public CommonResult getPostInFolder(
        @RequestParam(required = false, value = "sort")
        String sort,
        @PathVariable
        Long folderId,
        @PageableDefault
        Pageable pageable
    ) {
        Long memberId = SecurityUtils.getMemberId();
        Slice<BoardResponseDto> boardResponseDto =
            boardService.getPostInFolder(memberId, sort, folderId, pageable);
        return responseService.getSingleResult(boardResponseDto);
    }

    @PatchMapping("/{boardId}")
    public CommonResult countView(
        @PathVariable
        Long boardId, HttpServletRequest request
    ) {
        String ipAddress = request.getRemoteAddr();
        String viewCountKey = "VIEW:" + boardId + ":" + ipAddress;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(viewCountKey))) {
            return responseService.getFailResult();
        }

        boardService.updateCountView(boardId, viewCountKey);

        return responseService.getSuccessResult();
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

    @Operation(summary = "게시판 조회")
    @GetMapping("/{boardId}")
    public CommonResult getBoardDetailResponse(
        @PathVariable("boardId")
        Long boardId,
        @AuthenticationPrincipal
        Long memberId) {
        BoardImageDetailResponse response = boardService.getBoardDtos(memberId, boardId);
        return responseService.getSingleResult(response);
    }

    @Operation(summary = "상품 조회")
    @GetMapping("/{boardId}/product")
    public CommonResult getProductResponse(
        @PathVariable("boardId")
        Long boardId) {
        ProductResponse productResponse = boardService.getProductResponse(boardId);

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

    @PatchMapping("/{boardId}/purchase")
    public CommonResult movePurchasePage(
        @PathVariable
        Long boardId,
        HttpServletRequest request
    ) {
        String ipAddress = request.getRemoteAddr();
        String purchaseCountKey = "PURCHASE:" + boardId + ":" + ipAddress;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(purchaseCountKey))) {
            return responseService.getFailResult();
        }

        boardService.adaptPurchaseReaction(boardId, purchaseCountKey);

        return responseService.getSuccessResult();
    }
}

