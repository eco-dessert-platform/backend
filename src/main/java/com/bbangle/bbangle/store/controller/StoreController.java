package com.bbangle.bbangle.store.controller;

import com.bbangle.bbangle.board.dto.BoardInfoDto;
import com.bbangle.bbangle.board.service.BoardService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import com.bbangle.bbangle.store.dto.StoreDetailStoreDto;
import com.bbangle.bbangle.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/stores")
public class StoreController {

    private final StoreService storeService;
    private final BoardService boardService;
    private final ResponseService responseService;

    @GetMapping("/{storeId}")
    public CommonResult getPopularBoardResponse(
        @PathVariable("storeId")
        Long storeId,
        @AuthenticationPrincipal
        Long memberId
    ) {
        StoreDetailStoreDto getStoreResponse = storeService.getStoreResponse(memberId, storeId);
        return responseService.getSingleResult(getStoreResponse);
    }

    @GetMapping("/{storeId}/boards/best")
    public CommonResult getPopularBoardResponses(
        @PathVariable("storeId")
        Long storeId,
        @AuthenticationPrincipal
        Long memberId
    ) {
        List<BoardInfoDto> popularBoardResponses = boardService.getTopBoardInfo(
            memberId, storeId);
        return responseService.getListResult(popularBoardResponses);
    }

    @GetMapping("/{storeId}/boards")
    public CommonResult getStoreAllBoard(
        @PathVariable("storeId")
        Long storeId,
        @RequestParam(value = "cursorId", required = false, defaultValue = "0")
        Long boardIdAsCursorId,
        @AuthenticationPrincipal
        Long memberId
    ) {
        CursorPageResponse<BoardInfoDto> boardListResponse = storeService.getBoardsInStore(memberId, storeId, boardIdAsCursorId);
        return responseService.getSingleResult(boardListResponse);
    }
}
