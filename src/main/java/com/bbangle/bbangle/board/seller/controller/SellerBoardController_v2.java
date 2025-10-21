package com.bbangle.bbangle.board.seller.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/seller/boards")
@RequiredArgsConstructor
public class SellerBoardController_v2 {
// TODO: 백업용 - 추후 삭제 예정
  /*  private final ResponseService responseService;
    private final BoardService boardService;
    private final BoardFacade boardFacade;
    private final SearchMapper searchMapper;
    private final BoardUploadService_v2 boardUploadService;

    @PostMapping("/board/{storeId}")
    @Override
    public CommonResult upload(@PathVariable Long storeId,
        @RequestBody BoardUploadRequest_v2 request) {
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
        SearchCommand.Main command = searchMapper.toSearchMain(filterRequest, sort, null, cursorId,
            memberId,
            limitSize);
        CursorPagination<SearchInfo.Select> searchBoardPage = boardFacade.getBoardList(command);
        return responseService.getSingleResult(searchBoardPage);
    }

    @GetMapping("/folders/{folderId}")
    public SingleResult<CursorPageResponse<BoardResponse>> getPostInFolder(
        @RequestParam(required = false, value = "sort")
        @Schema(description = "위시리스트 상품 정렬 타입")
        FolderBoardSortType sort,
        @PathVariable(value = "folderId")
        Long folderId,
        @RequestParam(value = "cursorId", required = false)
        @Schema(description = "커서 아이디")
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
    }*/
}

