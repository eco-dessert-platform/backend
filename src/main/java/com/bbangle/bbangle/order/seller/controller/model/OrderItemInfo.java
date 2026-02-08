package com.bbangle.bbangle.order.seller.controller.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record OrderItemInfo(

    @Schema(description = "상품명", example = "예제 상품")
    @NotBlank
    String itemName,
    @Schema(description = "상품수량", example = "2")
    @Positive
    Integer quantity,
    @Schema(description = "상품금액(원)", example = "50000")
    @PositiveOrZero
    Long unitPrice,
    @Schema(description = "총 금액(원)", example = "100000")
    @PositiveOrZero
    Long totalPrice

) {
}
