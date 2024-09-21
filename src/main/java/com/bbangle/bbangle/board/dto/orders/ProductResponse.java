package com.bbangle.bbangle.board.dto.orders;

import com.bbangle.bbangle.board.dto.orders.abstracts.ProductOrderResponseBase;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductResponse {
    List<ProductOrderResponseBase> products;
    Boolean boardIsBundled;
    Boolean isSoldOut;
}
