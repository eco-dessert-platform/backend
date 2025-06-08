package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.constant.FolderBoardSortType;
import com.bbangle.bbangle.board.constant.SortType;
import com.bbangle.bbangle.board.dto.BoardResponse;
import com.bbangle.bbangle.board.dto.BoardUploadRequest;
import com.bbangle.bbangle.board.dto.FilterRequest;
import com.bbangle.bbangle.board.facade.BoardFacade;
import com.bbangle.bbangle.board.service.BoardService;
import com.bbangle.bbangle.board.service.BoardUploadService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.search.controller.mapper.SearchMapper;
import com.bbangle.bbangle.search.service.dto.SearchCommand;
import com.bbangle.bbangle.search.service.dto.SearchInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private final BoardFacade boardFacade;
    private final SearchMapper searchMapper;
    private final BoardUploadService boardUploadService;

    @PostMapping("/board/{storeId}")
    public CommonResult upload(@PathVariable Long storeId,
                               @RequestBody BoardUploadRequest request) {
        boardUploadService.upload(storeId, request);
        return responseService.getSuccessResult();
    }

    @Operation(summary = "게시글 전체 조회")
    @GetMapping
    public SingleResult<CursorPagination<SearchInfo.Select>> getList(
            @ParameterObject
            FilterRequest filterRequest,
            @RequestParam(required = false, defaultValue = "RECOMMEND")
            SortType sort,
            @RequestParam(required = false)
            Long cursorId,
            @Parameter(
                    description = "최대 30까지 입력 가능합니다.",
                    schema = @Schema(defaultValue = "30", maximum = "30")
            )
            @RequestParam(required = false, defaultValue = "30")
            Long limitSize,
            @AuthenticationPrincipal
            Long memberId
    ) {
        SearchCommand.Main command = searchMapper.toSearchMain(filterRequest, sort, null, cursorId, memberId, limitSize);
        CursorPagination<SearchInfo.Select> searchBoardPage = boardFacade.getBoardList(command);
        return responseService.getSingleResult(searchBoardPage);
    }

    @GetMapping("/folders/{folderId}")
    public SingleResult<CursorPageResponse<BoardResponse>> getPostInFolder(
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

