package com.bbangle.bbangle.fixture.board.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.store.domain.Store;

public final class ProductFixture {
    private ProductFixture() {
    }

    public static Product create(Board board, String title) {
        return Product.builder()
            .title(title)
            .board(board)
            .build();
    }

    public static Product defaultProductWithStore(Store store) {
        return Product.builder()
            .store(store)
            .title("테스트 상품")
            .price(10000)
            .build();
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
