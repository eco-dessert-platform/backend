package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.controller.dto.StoreResponse;
import com.bbangle.bbangle.board.controller.mapper.StoreMapper;
import com.bbangle.bbangle.board.facade.StoreFacade;
import com.bbangle.bbangle.board.service.dto.StoreInfo;
import com.bbangle.bbangle.board.service.dto.StoreInfo.AllBoard;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/stores")
public class StoreController {

        private final StoreFacade storeFacade;
        private final StoreMapper storeMapper;
        private final ResponseService responseService;

        @GetMapping("/{storeId}")
        public CommonResult getPopularBoardResponse(
            @PathVariable("storeId")
            Long storeId,
            @AuthenticationPrincipal
            Long memberId
        ) {
                StoreInfo.StoreDetail storeDetail = storeFacade.getStoreDetail(memberId, storeId);
                StoreResponse.StoreDetail storeDetailResponse = storeMapper.toStoreDetailResponse(
                    storeDetail);
                return responseService.getSingleResult(storeDetailResponse);
        }

        @GetMapping("/{storeId}/boards/best")
        public CommonResult getPopularBoardResponses(
            @PathVariable("storeId")
            Long storeId,
            @AuthenticationPrincipal
            Long memberId
        ) {
                List<StoreInfo.BestBoard> popularBoardResponses = storeFacade.getBestBoards(
                    memberId, storeId);
                return responseService.getListResult(popularBoardResponses);
        }

    @GetMapping("/{storeId}/boards")
    public SingleResult<CursorPageResponse<AllBoard>> getStoreAllBoard(
            @PathVariable("storeId")
            Long storeId,
            @RequestParam(value = "cursorId", required = false)
            Long boardIdAsCursorId,
            @AuthenticationPrincipal
            Long memberId
    ) {
        CursorPageResponse<AllBoard> response = storeFacade.getAllBoard(memberId, storeId, boardIdAsCursorId);
        return responseService.getSingleResult(response);
    }
}
