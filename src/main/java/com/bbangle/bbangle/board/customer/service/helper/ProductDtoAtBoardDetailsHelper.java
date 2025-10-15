package com.bbangle.bbangle.board.customer.service.helper;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.customer.dto.orders.ProductDtoAtBoardDetail;
import com.bbangle.bbangle.push.domain.Push;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductDtoAtBoardDetailsHelper {

    public static List<ProductDtoAtBoardDetail> getDtoList(List<Product> products, Map<Long, Push> pushMap) {
        return products.stream()
            .map(product -> {
                Push push = pushMap.get(product.getId());
                return ProductDtoAtBoardDetail.of(product, push);
            }).toList();
    }

    public static List<ProductDtoAtBoardDetail> getDtoList(List<Product> products) {
        return products.stream()
            .map(ProductDtoAtBoardDetail::of)
            .toList();
    }
}