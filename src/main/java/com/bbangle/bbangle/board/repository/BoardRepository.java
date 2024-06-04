package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.dto.ProductDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardQueryDSLRepository {
    List<String> getProductDtosToDuplicatedTags(List<ProductDto> productDtos);
}
