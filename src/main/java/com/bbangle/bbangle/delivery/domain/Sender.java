package com.bbangle.bbangle.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Embeddable
public class Sender {

    @Column(name = "sender_name", length = 20)
    private String senderName;

    @Column(name = "sender_phone", length = 20)
    private String senderPhone;

    @Column(name = "sender_address", length = 200)
    private String senderAddress;

    @Column(name = "sender_address_detail", length = 200)
    private String senderAddressDetail;

    @Column(name = "sender_zip_code", length = 10)
    private String senderZipCode;

    private Sender(String senderName, String senderPhone, String senderAddress,
                   String senderAddressDetail, String senderZipCode) {
        this.senderName = senderName;
        this.senderPhone = senderPhone;
        this.senderAddress = senderAddress;
        this.senderAddressDetail = senderAddressDetail;
        this.senderZipCode = senderZipCode;
    }

    public static Sender of(String name, String phone, String address,
                            String addressDetail, String zipCode) {
        return new Sender(name, phone, address, addressDetail, zipCode);
    }

}
