package com.bbangle.bbangle.payment.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaymentConfirmRequest(
    @NotBlank String paymentKey,
    @NotBlank String orderNumber,
    @NotNull @Positive Long amount
) {

}
