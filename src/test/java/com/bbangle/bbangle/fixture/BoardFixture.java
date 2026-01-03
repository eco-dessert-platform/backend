package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.store.domain.Store;
import java.util.ArrayList;

public final class BoardFixture {

    private BoardFixture() {
    }

    public static Board defaultBoard() {
        return Board.builder()
            .id(1L)
            .title("상품명")
            .price(10_000)
            .products(new ArrayList<>())
            .productImgs(new ArrayList<>())
            .store(StoreFixture.defaultStore())
            .build();
    }

    public static Board defaultBoard(String title) {
        return Board.builder()
            .title(title)
            .price(10_000)
            .discountRate(0)
            .deliveryFee(0)
            .store(StoreFixture.defaultStore())
            .products(new ArrayList<>())
            .productImgs(new ArrayList<>())
            .build();
    }

    public static Board defaultBoard(Store store, String title) {
        return Board.builder()
            .title(title)
            .price(10_000)
            .discountRate(0)
            .deliveryFee(0)
            .store(store)
            .products(new ArrayList<>())
            .productImgs(new ArrayList<>())
            .build();
    }

}
