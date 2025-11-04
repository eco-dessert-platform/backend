package com.bbangle.bbangle.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Embeddable
public class Shipping {

    @Column(length = 255)
    private String deliveryMemo;

    @Column(length = 50)
    private String courierName;

    @Column(length = 50)
    private String trackingNumber;

    private Integer fee;

    @Column(name = "status", length = 30, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private ShippingStatus status;

    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

}
