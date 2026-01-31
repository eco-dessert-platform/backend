package com.bbangle.bbangle.fixture.order;

import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import org.springframework.test.util.ReflectionTestUtils;

public class OrderItemFixture {

    private OrderItemFixture() {
    }

    public static OrderItem orderItemWithStatus(OrderStatus status) {
        return OrderItem.builder()
            .quantity(1)
            .productPrice(10000)
            .unitPrice(10000)
            .totalPrice(10000)
            .orderStatus(status)
            // .orderDeliveryStatus(OrderDeliveryStatus.READY)  // enum 값 있으면 세팅
            // .order(order)                                   // 필요 시
            // .product(product)                               // 필요 시
            .build();
    }

    public static OrderItem paymentCompleted() {
        return orderItemWithStatus(OrderStatus.PAYMENT_COMPLETED);
    }

    public static OrderItem orderConfirmed() {
        return orderItemWithStatus(OrderStatus.ORDER_CONFIRMED);
    }

    public static OrderItem statusNull() {
        return orderItemWithStatus(null);
    }
    
    public static OrderItem withId(OrderItem orderItem, Long id) {
        ReflectionTestUtils.setField(orderItem, "id", id);
        return orderItem;
    }

}
