package com.bbangle.bbangle.seller.seller.service.command;

import lombok.Builder;

@Builder
public record SellerCreateCommand(
        String storeName,
        String phoneNumber,
        String subPhoneNumber,
        String email,
        Long verificationNumber,
        String originAddress,
        String originAddressDetail
) {

}
