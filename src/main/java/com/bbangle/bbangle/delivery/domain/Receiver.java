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

    @Column(length = 20)
    private String recipientName;

    @Column(length = 20)
    private String recipientPhone1;

    @Column(length = 20)
    private String recipientPhone2;

    @Column(length = 200)
    private String recipientAddress;

    @Column(length = 200)
    private String recipientAddressDetail;

    @Column(length = 10)
    private String recipientZipCode;

}
