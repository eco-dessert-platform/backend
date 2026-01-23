package com.bbangle.bbangle.fixture.board.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;

public final class ProductFixture {
    private ProductFixture() {
    }

    public static Product create(Board board, String title) {
        Product product = Product.builder()
            .title(title)
            .board(board)
            .build();
        return product;
    }

    public static Product createWithStock(Board board, String title, int stock) {
        return Product.builder()
            .title(title)
            .board(board)
            .stock(stock)
            .soldout(false)
            .build();
    }
}
