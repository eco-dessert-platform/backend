package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.recommend.service.RecommendBoardService;
import com.bbangle.bbangle.board.service.BoardService;
import com.bbangle.bbangle.board.service.ProductService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.page.CustomPage;
import com.bbangle.bbangle.review.service.ReviewService;
import com.bbangle.bbangle.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final ResponseService responseService;
    private final BoardService boardService;
    private final ProductService productService;
    private final StoreService storeService;
    private final ReviewService reviewService;
    private final RecommendBoardService recommendBoardService;

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
        if(memberId != null && sort == SortType.RECOMMEND){
            BoardCustomPage<List<BoardResponseDto>> boardResponseList = recommendBoardService.getBoardList(
                filterRequest,
                cursorId,
                memberId);

            return responseService.getSingleResult(boardResponseList);
        }
        BoardCustomPage<List<BoardResponseDto>> boardResponseList = boardService.getBoardList(
            filterRequest,
            sort,
            cursorId,
            memberId);
        return responseService.getSingleResult(boardResponseList);
    }

    @GetMapping("/count")
    public CommonResult getCount(
        @ParameterObject
        FilterRequest filterRequest
    ) {
        Long boardCount = boardService.getFilteredBoardCount(filterRequest);
        return responseService.getSingleResult(boardCount);
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

    private SortType settingDefaultSortTypeIfNull(SortType sort) {
        if (sort == null) {
            sort = SortType.RECOMMEND;
        }

        return sort;
    }

}

