package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.ProductImg;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImgRepository extends JpaRepository<ProductImg, Long> {
    List<ProductImg> findAllByIdInOrderByIdAsc(List<Long> imageIds);
}
