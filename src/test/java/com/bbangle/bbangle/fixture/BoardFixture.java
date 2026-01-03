package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.domain.Board;
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
}
