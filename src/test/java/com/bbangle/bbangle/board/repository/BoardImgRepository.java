package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.ProductImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardImgRepository extends JpaRepository<ProductImg, Long> {

}
