package com.bbangle.bbangle.fixture.board.domain;

import com.bbangle.bbangle.board.domain.ProductInfoNotice;

public final class ProductInfoNoticeFixture {
    private ProductInfoNoticeFixture() {
    }

    public static ProductInfoNotice defaultNotice() {
        return ProductInfoNotice.builder()
            .productName("productName")
            .build();
    }
}
