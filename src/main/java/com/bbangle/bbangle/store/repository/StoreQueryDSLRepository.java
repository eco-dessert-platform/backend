package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.board.customer.dto.AiLearningStoreDto;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.store.domain.Store;
import java.util.List;
import java.util.Optional;

public interface StoreQueryDSLRepository {

    Optional<Store> findByBoardId(Long boardId);

    List<AiLearningStoreDto> findAiLearningData();

    List<Board> findBestBoards(Long storeId);

    List<Board> findBoards(Long storeId, Long boardIdAsCursorId);

    Optional<Store> findByStoreName(String storeName);

    List<Store> findByNameContaining(String name);

}
