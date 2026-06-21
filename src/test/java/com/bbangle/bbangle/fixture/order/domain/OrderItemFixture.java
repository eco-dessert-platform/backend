package com.bbangle.bbangle.fixture.order.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.store.domain.Store;
import java.util.ArrayList;

public final class OrderItemFixture {

    private OrderItemFixture() {
    }

    public static OrderItem.OrderItemBuilder defaultOrderItem() {
        return OrderItem.builder()
            .quantity(2)
            .productPrice(25000)
            .unitPrice(25000)
            .totalPrice(50000)
            .orderStatus(OrderStatus.PAYMENT_COMPLETED)
            .orderDeliveryStatus(OrderDeliveryStatus.PREPARING);
    }

    public static OrderItem createDefaultOrderItem() {
        return defaultOrderItem().build();
    }

    public static OrderItem createOrderItemWithStatus(OrderStatus status) {
        return defaultOrderItem()
            .orderStatus(status)
            .build();
    }

    public static OrderItem createOrderItemWithQuantity(Integer quantity) {
        return defaultOrderItem()
            .quantity(quantity)
            .build();
    }

    public static OrderItem createOrderItemWithPrice(Integer unitPrice, Integer totalPrice) {
        return defaultOrderItem()
            .unitPrice(unitPrice)
            .totalPrice(totalPrice)
            .build();
    }

    public static OrderItem createOrderItemWithOrder(Order order) {
        OrderItem orderItem = defaultOrderItem()
            .order(order)
            .build();
        if (order != null) {
            order.addOrderItem(orderItem);
        }
        return orderItem;
    }

    public static OrderItem createOrderItemWithStatusAndOrder(OrderStatus status, Order order) {
        OrderItem orderItem = defaultOrderItem()
            .orderStatus(status)
            .order(order)
            .build();
        if (order != null) {
            order.addOrderItem(orderItem);
        }
        return orderItem;
    }

    public static OrderItem createOrderItemWithProduct(Product product) {
        return defaultOrderItem()
            .product(product)
            .build();
    }

    public static OrderItem createWithProductAndStatus(
        Order order, Store store, String productTitle, int price, OrderStatus status
    ) {
        Board board = BoardFixture.defaultBoardWithStore(store, productTitle);
        Product product = ProductFixture.create(board, productTitle, price);
        OrderItem item = defaultOrderItem()
            .orderStatus(status)
            .totalPrice(price)
            .product(product)
            .build();
        order.addOrderItem(item);
        return item;
    }

}
