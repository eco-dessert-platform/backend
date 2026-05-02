package com.bbangle.bbangle.fixture.order;

import com.bbangle.bbangle.board.domain.Product;
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
            .build();
    }

    public static OrderItem returnRequestedWithProduct(Product product) {
        return OrderItem.builder()
            .product(product)
            .quantity(1)
            .productPrice(10000)
            .unitPrice(10000)
            .totalPrice(10000)
            .orderStatus(OrderStatus.RETURN_REQUESTED)
            .build();
    }

    public static OrderItem cancelRequestedWithProduct(Product product) {
        return OrderItem.builder()
            .product(product)
            .quantity(1)
            .productPrice(10000)
            .unitPrice(10000)
            .totalPrice(10000)
            .orderStatus(OrderStatus.CANCEL_REQUESTED)
            .build();
    }

    public static OrderItem returnApprovedWithProduct(Product product) {
        return OrderItem.builder()
            .product(product)
            .quantity(1)
            .productPrice(10000)
            .unitPrice(10000)
            .totalPrice(10000)
            .orderStatus(OrderStatus.RETURN_APPROVED)
            .build();
    }

    public static OrderItem paymentCompleted() {
        return orderItemWithStatus(OrderStatus.PAYMENT_COMPLETED);
    }

    public static OrderItem orderConfirmed() {
        return orderItemWithStatus(OrderStatus.ORDER_CONFIRMED);
    }

    public static OrderItem orderPayed() {
        return orderItemWithStatus(OrderStatus.PAYMENT_COMPLETED);
    }

    public static OrderItem orderCancelRequested() {
        return orderItemWithStatus(OrderStatus.CANCEL_REQUESTED);
    }

    public static OrderItem orderReturnRequested() {
        return orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
    }

    public static OrderItem statusNull() {
        return orderItemWithStatus(null);
    }

    public static OrderItem withId(OrderItem orderItem, Long id) {
        ReflectionTestUtils.setField(orderItem, "id", id);
        return orderItem;
    }

}
