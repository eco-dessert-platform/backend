package com.bbangle.bbangle.order.customer.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderPreviewRequest(
    @NotEmpty @Valid List<OrderProductItem> items
) {

    public record OrderProductItem(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
    ) {}

}
