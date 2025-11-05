package com.bbangle.bbangle.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Embeddable
public class ShippingInfo {

    @Column(length = 255)
    private String deliveryMemo;

    @Column(length = 50)
    private String courierName;

    @Column(length = 50)
    private String trackingNumber;

    private Integer fee;

    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;


}
