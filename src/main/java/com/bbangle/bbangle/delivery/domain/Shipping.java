package com.bbangle.bbangle.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Embeddable
public class Shipping {

    @Embedded
    private ShippingInfo shippingInfo;

    @Column(name = "status", length = 30, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private ShippingStatus status;



}
