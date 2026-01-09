package com.bbangle.bbangle.fixture.board.admin.controller.dto;

import com.bbangle.bbangle.board.admin.controller.dto.AdminProductResponse;
import java.util.List;

public final class AdminProductResponseFixture {

    private AdminProductResponseFixture() {
    }

    public static AdminProductResponse defaultResponse() {
        return new AdminProductResponse(
            1L,
            "스토어명",
            "상품명",
            10000,
            List.of()
        );
    }

}
