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

    /**
     * 주문 생성 시점의 배송 정보. 운송장은 아직 없고 배송 요청사항만 채운다.
     * (배송비는 주문 단위 값이므로 {@code Order.deliveryFee} 가 보관한다)
     */
    public static Shipping ofOrder(String deliveryMemo) {
        Shipping shipping = new Shipping(null, null, null);
        shipping.deliveryMemo = deliveryMemo;
        return shipping;
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

}
