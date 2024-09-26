package com.bbangle.bbangle.board.service.util;

import com.bbangle.bbangle.board.domain.Product;
import java.util.List;
import lombok.Getter;

@Getter
public class ProductsUtil {
    private static final int ONE_CATEGORY = 1;

    @Getter
    private boolean isBundled = false;

    public static List<Long> getIds(List<Product> products) {
        return  products.stream()
            .map(Product::getId)
            .toList();
    }

    public static boolean getIsSoldOut(List<Product> products) {
        return products.stream()
            .noneMatch(product -> !product.isSoldout());
    }

    public static boolean getIsBundled(List<Product> products) {
        return products.stream()
            .map(Product::getCategory)
            .distinct()
            .count() > ONE_CATEGORY;
    }
}
