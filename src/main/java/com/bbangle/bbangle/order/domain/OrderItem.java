package com.bbangle.bbangle.order.domain;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.bbangle.bbangle.order.domain.model.OrderStatus.RETURN_REQUESTED;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "order_item")
@Entity
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "product_price")
    private Integer productPrice;

    @Column(name = "unit_price")
    private Integer unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", columnDefinition = "VARCHAR(20)")
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", columnDefinition = "VARCHAR(20)")
    private OrderDeliveryStatus orderDeliveryStatus;

    @Column(name = "total_price")
    private Integer totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public boolean confirmOrder() {
        if (this.orderStatus != OrderStatus.PAYMENT_COMPLETED) {
            return false;
        }
        this.orderStatus = OrderStatus.ORDER_CONFIRMED;
        return true;
    }

    @Builder
    public OrderItem(
            Integer quantity,
            Integer productPrice,
            Integer unitPrice,
            OrderStatus orderStatus,
            OrderDeliveryStatus orderDeliveryStatus,
            Integer totalPrice,
            Order order,
            Product product
    ) {
        this.quantity = quantity;
        this.productPrice = productPrice;
        this.unitPrice = unitPrice;
        this.orderStatus = orderStatus;
        this.orderDeliveryStatus = orderDeliveryStatus;
        this.totalPrice = totalPrice;
        this.order = order;
        this.product = product;
    }

    public void shipOrder() {
        this.orderStatus = OrderStatus.SHIPPED;
    }


    public void returnApprove() {
        if (orderStatus != RETURN_REQUESTED) {
            throw new BbangleException(BbangleErrorCode.ORDER_INVALID_STATUS);
        }
        this.orderStatus = OrderStatus.RETURN_APPROVED;
    }

    public void returnReject() {
        if (orderStatus != RETURN_REQUESTED) {
            throw new BbangleException(BbangleErrorCode.ORDER_INVALID_STATUS);
        }
        this.orderStatus = OrderStatus.RETURN_REJECTED;
    }
}
