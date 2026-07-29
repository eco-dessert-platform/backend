package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.board.customer.dto.AiLearningStoreDto;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.common.page.CursorPagination;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.seller.service.model.SellerStoreInfo.StoreInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreQueryDSLRepository {

    Optional<Store> findByBoardId(Long boardId);

    List<AiLearningStoreDto> findAiLearningData();

    List<Board> findBestBoards(Long storeId);

    List<Board> findBoards(Long storeId, Long boardIdAsCursorId);

    Optional<Store> findByStoreName(String storeName);

    Optional<Store> findByStoreNameAndIsNotDeleted(String storeName);

    boolean existsByStoreName(String name);

    CursorPagination<StoreInfo> findByStoreNameWithCursor(String storeName, Long cursorId);

    /**
     * 관리자용 스토어명 검색 — 삭제되지 않은 스토어만, 공백 무시 부분일치, id 오름차순 페이징
     * normalizedStoreName 이 빈 문자열이면 전체 조회
     */
    Page<Store> findActiveStoresByName(String normalizedStoreName, Pageable pageable);

    boolean existsByNormalizedStoreName(String normalizedStoreName);
}
