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
public class Shipping {

    @Column(name = "courier_name", length = 50)
    private String courierName;

    @Column(name = "tracking_number", length = 50)
    private String trackingNumber;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "fee")
    private Integer fee;

    @Column(name = "delivery_memo", length = 255)
    private String deliveryMemo;

    private Shipping(String courierName, String trackingNumber, LocalDateTime shippedAt) {
        this.courierName = courierName;
        this.trackingNumber = trackingNumber;
        this.shippedAt = shippedAt;
    }

    public static Shipping of(String courierName, String trackingNumber) {
        return new Shipping(courierName, trackingNumber, LocalDateTime.now());
    }

    public static Shipping empty() {
        return new Shipping(null, null, null);
    }

    public void updateShippingInfo(String courierName, String trackingNumber) {
        this.courierName = courierName;
        this.trackingNumber = trackingNumber;
        this.shippedAt = LocalDateTime.now();
    }

}
