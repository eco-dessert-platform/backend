package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.page.StoreDetailCustomPage;
import com.bbangle.bbangle.store.dto.PopularBoardResponse;
import com.bbangle.bbangle.store.dto.StoreBoardsResponse;
import com.bbangle.bbangle.page.StoreCustomPage;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.dto.StoreResponse;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import java.util.List;

import java.util.HashMap;


public interface StoreQueryDSLRepository {

    StoreDto findByBoardId(Long boardId);

    StoreResponse getStoreResponse(Long meberId, Long storeId);

    List<PopularBoardResponse> getPopularBoardResponses(Long memberId, Long storeId);

    StoreDetailCustomPage<List<StoreBoardsResponse>> getStoreBoardsResponse(Long memberId, Long storeId,
        Long boardIdAsCursorId);

    HashMap<Long, String> getAllStoreTitle();

    StoreCustomPage<List<StoreResponseDto>> getStoreList(Long cursorId, Long memberId);
}