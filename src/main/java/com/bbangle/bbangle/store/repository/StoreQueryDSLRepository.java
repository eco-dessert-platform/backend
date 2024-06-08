package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.page.StoreCustomPage;
import com.bbangle.bbangle.store.dto.StoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.StoreResponseDto;
import java.util.List;

import java.util.HashMap;


public interface StoreQueryDSLRepository {

    StoreDetailStoreDto getStoreResponse(Long meberId, Long storeId);

    HashMap<Long, String> getAllStoreTitle();

    StoreCustomPage<List<StoreResponseDto>> getStoreList(Long cursorId, Long memberId);
}