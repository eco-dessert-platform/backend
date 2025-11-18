package com.bbangle.bbangle.seller.seller.service.command;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import lombok.Builder;

public record SellerCreateCommand(
        String storeName,
        String phoneNumber,
        String subPhoneNumber,
        String email,
        String originAddress,
        String originAddressDetail
) {

    @Builder
    public SellerCreateCommand(String storeName, String phoneNumber, String subPhoneNumber,
        String email, String originAddress, String originAddressDetail) {
        this.storeName = storeName;
        this.phoneNumber = phoneNumber;
        this.subPhoneNumber = subPhoneNumber;
        this.email = email;
        this.originAddress = originAddress;
        this.originAddressDetail = originAddressDetail;
        validate();
    }

    private void validate() {
        if(storeName == null || storeName.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_STORE_NAME);
        }
        if(phoneNumber == null || phoneNumber.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_PHONE_NUMBER);
        }
        if (subPhoneNumber == null || subPhoneNumber.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_PHONE_NUMBER);
        }
        if(email == null || email.isEmpty() || !email.contains("@")) {
            throw new BbangleException(BbangleErrorCode.INVALID_EMAIL);
        }
        if(originAddress == null || originAddress.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_ADDRESS);
        }
        if(originAddressDetail == null || originAddressDetail.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_DETAIL_ADDRESS);
        }
    }


}
