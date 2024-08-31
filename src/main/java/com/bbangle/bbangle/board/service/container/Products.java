package com.bbangle.bbangle.board.service.container;

import com.bbangle.bbangle.board.domain.Product;
import java.util.List;
import lombok.Getter;

@Getter
public class Products {
    private final int ONE_CATEGORY = 1;
    private List<Product> products;

    @Getter
    private boolean isBundled = false;

    public Products(List<Product> products) {
        this.products = products;
    }

    public static Products fromList(List<Product> products) {
        return new Products(products);
    }

    public List<Long> getIds() {
        return  products.stream()
            .map(Product::getId)
            .toList();
    }

    public boolean getIsSoldOut() {
        return products.stream()
            .noneMatch(product -> !product.isSoldout());
    }

    public boolean getIsBundled() {
        return products.stream()
            .map(Product::getCategory)
            .distinct()
            .count() > ONE_CATEGORY;
    }
}
