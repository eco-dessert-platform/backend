package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.page.StoreCustomPage;
import com.bbangle.bbangle.store.dto.StoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.StoreDto;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import java.util.List;

public interface StoreQueryDSLRepository {

    StoreDto findByBoardId(Long boardId);

    StoreDetailStoreDto getStoreResponse(Long meberId, Long storeId);

    StoreCustomPage<List<StoreResponseDto>> getStoreList(Long cursorId, Long memberId);
}