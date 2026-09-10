package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Product;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductQueryDSLRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findWithLockById(Long id);

    @Query("SELECT p FROM Board b JOIN b.products p WHERE b.id = :boardId")
    List<Product> findByBoardId(@Param("boardId") Long boardId);

    @Query("SELECT p FROM Product p WHERE p.board.id IN :boardIds AND p.isDeleted = false")
    List<Product> findByBoardIds(@Param("boardIds") List<Long> boardId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Product p
            SET p.isDeleted = true
            WHERE p.board.id IN :boardIds
        """)
    void softDeleteByBoardIds(@Param("boardIds") List<Long> boardIds);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Product p
            SET p.isDeleted = true
            WHERE p.board.id = :boardId
        """)
    void softDeleteByBoardId(@Param("boardId") Long boardId);

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Product p
            SET p.isDeleted = true
            WHERE p.id IN :productIds
        """)
    void softDeleteByProductIds(@Param("productIds") List<Long> productIds);

    /**
     * 게시글 상세 조회용: 삭제되지 않은 상품 옵션 목록.
     * Nutrition은 @Embedded라서 추가 조인 없이 함께 조회되고,
     * segmentIntolerances(OneToMany, LAZY)는 접근하지 않으므로 추가 쿼리가 발생하지 않는다.
     */
    @Query("SELECT p FROM Product p WHERE p.board.id = :boardId AND p.isDeleted = false")
    List<Product> findAllByBoardIdAndIsDeletedFalse(@Param("boardId") Long boardId);
}
