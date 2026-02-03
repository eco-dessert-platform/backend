package com.bbangle.bbangle.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Embeddable
public class Receiver {

    @Column(name = "recipient_name", length = 20)
    private String recipientName;

    @Column(name = "recipient_phone1", length = 20)
    private String recipientPhone1;

    @Column(name = "recipient_phone2", length = 20)
    private String recipientPhone2;

    @Column(name = "recipient_address", length = 200)
    private String recipientAddress;

    @Column(name = "recipient_address_detail", length = 200)
    private String recipientAddressDetail;

    @Column(name = "recipient_zip_code", length = 10)
    private String recipientZipCode;

    private Receiver(String recipientName, String recipientPhone1, String recipientPhone2,
                     String recipientAddress, String recipientAddressDetail, String recipientZipCode) {
        this.recipientName = recipientName;
        this.recipientPhone1 = recipientPhone1;
        this.recipientPhone2 = recipientPhone2;
        this.recipientAddress = recipientAddress;
        this.recipientAddressDetail = recipientAddressDetail;
        this.recipientZipCode = recipientZipCode;
    }

    public static Receiver of(String name, String phone1, String phone2,
                              String address, String addressDetail, String zipCode) {
        return new Receiver(name, phone1, phone2, address, addressDetail, zipCode);
    }

}
