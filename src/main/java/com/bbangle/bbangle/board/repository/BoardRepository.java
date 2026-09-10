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

    /**
     * 상세 조회용: 단일 값 연관관계(productInfoNotice, boardDetail)만 EntityGraph로 함께 조회.
     * products, productImgs는 컬렉션이라 카티션 곱을 피하기 위해 여기 포함하지 않고 별도 쿼리로 조회한다.
     * isDeleted = false 조건까지 메서드명에 포함해 soft-delete 된 게시글은 조회되지 않도록 한다.
     */
    @EntityGraph(attributePaths = {"productInfoNotice", "boardDetail"})
    Optional<Board> findByIdAndIsDeletedFalse(Long boardId);
}
