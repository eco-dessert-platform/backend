package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.customer.repository.ProductQueryDSLRepository;
import com.bbangle.bbangle.board.domain.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductQueryDSLRepository {

    @Query("SELECT p FROM Board b JOIN b.products p WHERE b.id = :boardId")
    List<Product> findByBoardId(@Param("boardId") Long boardId);
}
