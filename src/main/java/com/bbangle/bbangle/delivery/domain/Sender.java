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

    @Column(length = 20)
    private String senderName;

    @Column(length = 20)
    private String senderPhone;

    @Column(length = 200)
    private String senderAddress;

    @Column(length = 200)
    private String senderAddressDetail;

    @Column(length = 10)
    private String senderZipCode;

}
