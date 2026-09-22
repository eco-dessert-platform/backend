package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.SaleStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardQueryDSLRepository {

    @EntityGraph(attributePaths = {"store", "boardDetail", "boardStatistic"})
    Optional<Board> findById(Long boardId);

    @EntityGraph(attributePaths = {"store", "productInfoNotice", "boardDetail"})
    Optional<Board> findWithAllById(Long boardId);

    @EntityGraph(attributePaths = {"boardStatistic"})
    Page<Board> findByIsCrawlingTrueAndIsDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"store", "boardStatistic"})
    Page<Board> findBySaleStatusAndIsDeletedFalse(SaleStatus saleStatus, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Board b
        SET b.isDeleted = true
        WHERE b.id IN :boardIds
        """)
    void softDeleteByIds(List<Long> boardIds);

    @EntityGraph(attributePaths = {"productInfoNotice", "boardDetail"})
    Optional<Board> findByIdAndIsDeletedFalse(Long boardId);

    /**
     * 게시글 복제 시 제목 중복 방지("제목 (n)")를 계산하기 위해,
     * 해당 스토어의 삭제되지 않은 게시글 제목 전체를 조회한다.
     */
    @Query("SELECT b.title FROM Board b WHERE b.store.id = :storeId AND b.isDeleted = false")
    List<String> findAllTitlesByStoreId(@Param("storeId") Long storeId);
}
