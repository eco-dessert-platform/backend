package com.bbangle.bbangle.fixture.board.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.ProductImg;

public final class ProductImgFixture {

    private ProductImgFixture() {}

    public static ProductImg defaultProductImgWithProduct(Board board) {
        return ProductImg.builder()
            .board(board)
            .url("image url")
            .imgOrder(1)
            .build();
    }

    public static ProductImg defaultProductImgWithProductAndOrder(Board board, String url, int order) {
        return ProductImg.builder()
            .board(board)
            .url(url)
            .imgOrder(order)
            .build();
    }

    public static ProductImg defaultProductImgThumbnail(Board board, String url) {
        return ProductImg.builder()
            .board(board)
            .url(url)
            .imgOrder(0)
            .build();
    }
}
