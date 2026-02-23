package com.bbangle.bbangle.order.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.delivery.domain.Receiver;
import com.bbangle.bbangle.delivery.domain.Sender;
import com.bbangle.bbangle.delivery.domain.Shipping;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "order_delivery")
@Entity
public class OrderDelivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Embedded
    private Sender sender;

    @Embedded
    private Receiver receiver;

    @Embedded
    private Shipping shipping;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private OrderDeliveryStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    /**
     * 양방향 연관관계 설정용 패키지 전용 메서드
     * OrderItem.addOrderDelivery()를 통해서만 호출되어야 합니다.
     */
    void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    private OrderDelivery(Sender sender, Receiver receiver, Shipping shipping,
                          OrderDeliveryStatus status, OrderItem orderItem) {
        this.sender = sender;
        this.receiver = receiver;
        this.shipping = shipping;
        this.status = status;
        this.orderItem = orderItem;
    }

    public static OrderDelivery create(Sender sender, Receiver receiver, Shipping shipping,
                                       OrderDeliveryStatus status, OrderItem orderItem) {
        return new OrderDelivery(sender, receiver, shipping, status, orderItem);
    }

    private static final Set<OrderDeliveryStatus> MODIFIABLE_STATUSES = Set.of(
        OrderDeliveryStatus.NONE,
        OrderDeliveryStatus.PREPARING,
        OrderDeliveryStatus.PICKING_UP
    );

    public void registerShipment(String courierName, String trackingNumber) {
        if (this.shipping == null) {
            this.shipping = Shipping.of(courierName, trackingNumber);
        } else {
            this.shipping.updateShippingInfo(courierName, trackingNumber);
        }
        this.status = OrderDeliveryStatus.DELIVERING;
    }

    public void modifyShipment(String courierName, String trackingNumber) {
        if (!MODIFIABLE_STATUSES.contains(this.status)) {
            throw new BbangleException(BbangleErrorCode.DELIVERY_MODIFY_NOT_ALLOWED);
        }
        this.shipping.modifyShippingInfo(courierName, trackingNumber);
    }

}
