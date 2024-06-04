package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductQueryDSLRepository {


}

