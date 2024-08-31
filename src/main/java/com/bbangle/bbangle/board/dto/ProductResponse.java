package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.dto.product.ProductOrderResponseBase;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {

    List<ProductOrderResponseBase> products;
    Boolean boardIsBundled;
    Boolean isSoldOut;
}
