package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.recommend.service.RecommendBoardService;
import com.bbangle.bbangle.board.service.BoardService;
import com.bbangle.bbangle.board.service.BoardUploadService;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import com.bbangle.bbangle.common.page.CustomPage;
import com.bbangle.bbangle.common.service.ResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/boards")
@Tag(name = "Boards", description = "게시판 API")
@RequiredArgsConstructor
public class BoardController {

    private final ResponseService responseService;
    private final BoardService boardService;
    private final RecommendBoardService recommendBoardService;

    @Operation(summary = "게시글 전체 조회")
    @GetMapping
    public CommonResult getList(
            @ParameterObject
            FilterRequest filterRequest,
            @RequestParam(required = false, defaultValue = "RECOMMEND")
            SortType sort,
            @RequestParam(required = false)
            Long cursorId,
            @AuthenticationPrincipal
            Long memberId
    ) {
        if (memberId != null && sort == SortType.RECOMMEND) {
            CursorPageResponse<BoardResponse> response = recommendBoardService.getBoardList(
                    filterRequest,
                    cursorId,
                    memberId);

            return responseService.getSingleResult(response);
        }
        CursorPageResponse<BoardResponse> response = boardService.getBoards(
                filterRequest,
                sort,
                cursorId,
                memberId);
        return responseService.getSingleResult(response);
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
        CursorPageResponse<BoardResponse> boardResponseDto = boardService.getPostInFolder(
                memberId, sort, folderId, cursorId);
        return responseService.getSingleResult(boardResponseDto);
    }
}

