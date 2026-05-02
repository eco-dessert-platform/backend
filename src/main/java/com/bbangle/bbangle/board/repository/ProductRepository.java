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
}
