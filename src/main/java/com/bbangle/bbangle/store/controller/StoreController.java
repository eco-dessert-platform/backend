package com.bbangle.bbangle.store.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.store.controller.dto.StoreResponse;
import com.bbangle.bbangle.store.controller.mapper.StoreMapper;
import com.bbangle.bbangle.store.facade.StoreFacade;
import com.bbangle.bbangle.store.service.dto.StoreInfo;
import com.bbangle.bbangle.store.service.dto.StoreInfo.AllBoard;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        public CommonResult getStoreAllBoard(
            @PathVariable("storeId")
            Long storeId,
            @RequestParam(value = "cursorId", required = false)
            Long boardIdAsCursorId,
            @AuthenticationPrincipal
            Long memberId
        ) {
                List<AllBoard> boardListResponse = storeFacade.getAllBoard(memberId, storeId,
                    boardIdAsCursorId);
                return responseService.getSingleResult(boardListResponse);
        }
}
