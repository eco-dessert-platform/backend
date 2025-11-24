package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.board.customer.dto.AiLearningStoreDto;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import java.util.List;
import java.util.Optional;

public interface StoreQueryDSLRepository {

    Optional<Store> findByBoardId(Long boardId);

    List<AiLearningStoreDto> findAiLearningData();

    List<Board> findBestBoards(Long storeId);

    List<Board> findBoards(Long storeId, Long boardIdAsCursorId);

    Optional<Store> findByStoreName(String storeName);

    List<Store> getStoreByStoreName(String storeName);

    CursorPagination<StoreInfo> findNextCursorPage(List<Long> storeIds);
}
