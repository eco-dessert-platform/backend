package com.bbangle.bbangle.push.customer.dto;

import com.bbangle.bbangle.push.domain.PushCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PushRequest(
    @NotNull
    @Schema(description = "상품 ID", example = "123")
    Long productId,
    @NotNull
    @Schema(description = "푸시 카테고리 타입", implementation = PushCategory.class)
    PushCategory pushCategory
) {

}
