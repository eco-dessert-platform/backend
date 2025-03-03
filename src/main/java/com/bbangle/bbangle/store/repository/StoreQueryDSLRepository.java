package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.board.dto.AiLearningStoreDto;
import com.bbangle.bbangle.store.dto.StoreDto;
import java.util.List;

public interface StoreQueryDSLRepository {

    StoreDto findByBoardId(Long boardId);

    List<AiLearningStoreDto> findAiLearningData();
}