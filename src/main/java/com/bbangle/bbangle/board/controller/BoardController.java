package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.dto.BoardImageDetailResponse;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.dto.ProductResponse;
import com.bbangle.bbangle.board.service.BoardService;
import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.page.CustomPage;
import com.bbangle.bbangle.review.dto.SummarizedReviewResponse;
import com.bbangle.bbangle.review.service.ReviewService;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.service.StoreService;
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
import org.springframework.data.redis.core.RedisTemplate;
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

    private static final String NON_VIEW_KEY = null;

    @Qualifier("defaultRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;
    private final ResponseService responseService;
    private final BoardService boardService;
    private final BoardStatisticService boardStatisticService;
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
        @RequestParam(required = false, value = "cursorId")
        Long cursorId,
        @AuthenticationPrincipal
        Long memberId
    ) {
        sort = settingDefaultSortTypeIfNull(sort);
        BoardCustomPage<List<BoardResponseDto>> boardResponseList = boardService.getBoardList(
            filterRequest,
            sort,
            cursorId,
            memberId);
        return responseService.getSingleResult(boardResponseList);
    }

    @GetMapping("/folders/{folderId}")
    public CommonResult getPostInFolder(
        @RequestParam(required = false, value = "sort")
        FolderBoardSortType sort,
        @PathVariable(value = "folderId")
        Long folderId,
        @RequestParam(value = "cursorId", required = false)
        Long cursorId,
        @AuthenticationPrincipal
        Long memberId
    ) {
        if (sort == null) {
            sort = FolderBoardSortType.WISHLIST_RECENT;
        }
        BoardCustomPage<List<BoardResponseDto>> boardResponseDto = boardService.getPostInFolder(
            memberId, sort, folderId, cursorId);
        return responseService.getSingleResult(boardResponseDto);
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
        Long memberId,
        HttpServletRequest httpServletRequest
    ) {
        String viewCountKey = getViewCountKey(boardId, httpServletRequest);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(viewCountKey))) {
            BoardDetailResponse response = boardService.getBoardDtos(memberId, boardId, NON_VIEW_KEY);
            return responseService.getSingleResult(response);
        }
        BoardDetailResponse response = boardService.getBoardDtos(memberId, boardId, viewCountKey);
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

    private static String getViewCountKey(Long boardId, HttpServletRequest httpServletRequest) {
        String ipAddress = httpServletRequest.getRemoteAddr();
        return "VIEW:" + boardId + ":" + ipAddress;
    }

    private SortType settingDefaultSortTypeIfNull(SortType sort) {
        if (sort == null) {
            sort = SortType.RECOMMEND;
        }

        return sort;
    }

}

