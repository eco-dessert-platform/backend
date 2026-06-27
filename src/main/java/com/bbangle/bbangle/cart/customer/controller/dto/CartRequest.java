package com.bbangle.bbangle.cart.customer.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CartRequest {

    public record DeleteCartOptionsRequest(
        @NotEmpty
        @Schema(description = "삭제할 장바구니 옵션 ID 목록")
        List<@NotNull Long> cartOptionIds
    ) {}

    public record AddCartRequest(
        @NotNull
        @Schema(description = "장바구니에 추가할 게시글(상품) Id", example = "1")
        Long boardId,
        @NotEmpty
        @Valid
        List<@NotNull @Valid SelectedOptions> options
    ) {
        public record SelectedOptions(
            @NotNull
            @Schema(description = "장바구니에 추가할 상품 옵션 Id", example = "1")
            Long productId,

            @Min(value = 1, message = "수량은 1개 이상 입력해주세요.")
            @Max(value = 999, message = "수량은 999개 이하로 입력해주세요.")
            @Schema(description = "장바구니에 추가할 상품 옵션의 수량", example = "1")
            int quantity
        ) {}
    }
}
