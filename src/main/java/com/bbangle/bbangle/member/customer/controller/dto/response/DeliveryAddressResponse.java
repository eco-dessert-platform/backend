package com.bbangle.bbangle.member.customer.controller.dto.response;

import com.bbangle.bbangle.member.domain.MemberDeliveryAddress;

public record DeliveryAddressResponse(
    Long id,
    String addressName,
    boolean isDefault,
    String recipientName,
    String phone,
    String address,
    String addressDetail,
    String zipCode
) {
    public static DeliveryAddressResponse from(MemberDeliveryAddress entity) {
        return new DeliveryAddressResponse(
            entity.getId(),
            entity.getAddressName(),
            entity.isDefault(),
            entity.getRecipientName(),
            entity.getPhone(),
            entity.getAddress(),
            entity.getAddressDetail(),
            entity.getZipCode()
        );
    }
}
