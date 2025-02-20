package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.board.dto.AiLearningStoreDto;
import com.bbangle.bbangle.store.dto.StoreDetailStoreDto;
import com.bbangle.bbangle.store.dto.StoreDto;
import java.util.List;

public interface StoreQueryDSLRepository {

    StoreDto findByBoardId(Long boardId);

    StoreDetailStoreDto getStoreResponse(Long meberId, Long storeId);

    List<AiLearningStoreDto> findAiLearningData();
}