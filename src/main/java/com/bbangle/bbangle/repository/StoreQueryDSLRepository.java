package com.bbangle.bbangle.repository;

import com.bbangle.bbangle.dto.StoreDetailResponseDto;
import com.bbangle.bbangle.dto.StoreResponseDto;
import com.bbangle.bbangle.model.Search;

import java.util.HashMap;
import java.util.List;

public interface StoreQueryDSLRepository {
    StoreDetailResponseDto getStoreDetailResponseDto(Long storeId);

    HashMap<Long, String> getAllStoreTitle();
}
