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

    @Column(length = 255)
    private String deliveryMemo;

    @Column(length = 50)
    private String courierName;

    @Column(length = 50)
    private String trackingNumber;

    private Integer fee;

    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    private Shipping(String courierName, String trackingNumber, LocalDateTime shippedAt) {
        this.courierName = courierName;
        this.trackingNumber = trackingNumber;
        this.shippedAt = shippedAt;
    }

    public static Shipping of(String courierName, String trackingNumber) {
        return new Shipping(courierName, trackingNumber, LocalDateTime.now());
    }
    
    public static Shipping scheduled(String courierName, String trackingNumber) {
        return new Shipping(courierName, trackingNumber, null);
    }

    public static Shipping empty() {
        return new Shipping(null, null, null);
    }

    public void updateShippingInfo(String courierName, String trackingNumber) {
        this.courierName = courierName;
        this.trackingNumber = trackingNumber;
        this.shippedAt = LocalDateTime.now();
    }

    public void modifyShippingInfo(String courierName, String trackingNumber) {
        this.courierName = courierName;
        this.trackingNumber = trackingNumber;
    }

    /**
     * 배송완료 시각을 기록합니다.
     * 배송완료 후 N일 자동 구매확정의 기산점으로 사용됩니다.
     */
    public void markDelivered(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

}
