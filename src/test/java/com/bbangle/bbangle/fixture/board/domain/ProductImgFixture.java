package com.bbangle.bbangle.fixture.board.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.ProductImg;

public final class ProductImgFixture {

    private void ProductImgFixture() {
    }

    public static ProductImg defaultProductImgWithProduct(Board board) {
        return ProductImg.builder()
            .board(board)
            .url("image url")
            .imgOrder(1)
            .build();
    }

}
