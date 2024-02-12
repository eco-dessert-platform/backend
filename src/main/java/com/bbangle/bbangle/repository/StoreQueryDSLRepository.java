package com.bbangle.bbangle.repository;

import com.bbangle.bbangle.dto.StoreDetailResponseDto;

public interface StoreQueryDSLRepository {
    StoreDetailResponseDto getStoreDetailResponseDto(Long storeId);
}
