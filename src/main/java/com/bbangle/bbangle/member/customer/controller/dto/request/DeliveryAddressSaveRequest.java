package com.bbangle.bbangle.member.customer.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeliveryAddressSaveRequest(
    @NotBlank @Size(max = 50) String addressName,
    @NotBlank @Size(max = 100) String recipientName,
    @NotBlank @Size(max = 20) String phone,
    @NotBlank @Size(max = 255) String address,
    @Size(max = 255) String addressDetail,
    @Size(max = 10) String zipCode,
    boolean isDefault
) {
}
