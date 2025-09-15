package com.bbangle.bbangle.board.controller;

import com.bbangle.bbangle.board.controller.dto.StoreResponse;
import com.bbangle.bbangle.board.controller.dto.StoreResponse.SearchResponse;
import com.bbangle.bbangle.board.controller.mapper.StoreMapper;
import com.bbangle.bbangle.board.facade.StoreFacade;
import com.bbangle.bbangle.board.service.dto.StoreInfo;
import com.bbangle.bbangle.board.service.dto.StoreInfo.AllBoard;
import com.bbangle.bbangle.common.dto.ListResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.page.CursorPageResponse;
import com.bbangle.bbangle.common.service.ResponseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Store", description = "스토어 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/stores")
public class StoreController {

    private final StoreFacade storeFacade;
    private final StoreMapper storeMapper;
    private final ResponseService responseService;

    @GetMapping("/{storeId}")
    public SingleResult<StoreResponse.StoreDetail> getPopularBoardResponse(
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
    public ListResult<StoreInfo.BestBoard> getPopularBoardResponses(
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

    @Operation(summary = "스토어 검색")
    @GetMapping("/search/{value}")
    public ListResult<StoreResponse.SearchResponse> search(
        @PathVariable("value")
        @Parameter(description = "검색어", example = "빵그리의 오븐")
        String value
    ) {
        List<StoreResponse.SearchResponse> response = new ArrayList<>();
        response.add(new SearchResponse(1L, "빵그리의 오븐 즉석빵 상점"));
        response.add(new SearchResponse(2L, "빵그리의 오븐 공장빵 상점"));
        return responseService.getListResult(response);
    }

}
