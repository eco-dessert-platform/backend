package com.bbangle.bbangle.board.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {

    Boolean boardIsBundled;
    List<ProductDto> products;

    public static ProductResponse of(
        Boolean boardIsBundled,
        List<ProductDto> products
    ) {
        return ProductResponse.builder()
            .boardIsBundled(boardIsBundled)
            .products(products)
            .build();
    }

}
