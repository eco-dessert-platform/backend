package com.bbangle.bbangle.order.customer.dto.request;

import com.bbangle.bbangle.payment.domain.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderCreateRequest(
    @NotEmpty @Valid List<OrderProductItem> items,
    Long deliveryAddressId,
    String deliveryRequest,
    @NotNull PaymentMethod paymentMethod
) {

    public record OrderProductItem(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity
    ) {}

}
