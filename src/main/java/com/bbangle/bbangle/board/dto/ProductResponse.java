package com.bbangle.bbangle.board.dto;

import java.util.List;
import java.util.Random;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {

    Boolean boardIsBundled;
    List<BoardDetailProductDto> products;

    public static ProductResponse of(
        Boolean boardIsBundled,
        List<BoardDetailProductDto> products
    ) {
        return ProductResponse.builder()
            .boardIsBundled(boardIsBundled)
            .products(products)
            .build();
    }

    // 프론트 API 연동 테스트를 위한 값입니다. 해당 기능 구현 시 삭제됩니다.
    public ProductResponse toFixture() {
        Random random = new Random();

        List<BoardDetailProductDto> boardDetailProductDtos = List.of(
            BoardDetailProductDto.builder().build().toFixture(),
            BoardDetailProductDto.builder().build().toFixture(),
            BoardDetailProductDto.builder().build().toFixture());

        if (random.nextBoolean()) {
            return ProductResponse.builder()
                .boardIsBundled(random.nextBoolean())
                .products(boardDetailProductDtos)
                .build();
        } else {
            return ProductResponse.builder()
                .boardIsBundled(random.nextBoolean())
                .products(boardDetailProductDtos)
                .build();
        }
    }
}
