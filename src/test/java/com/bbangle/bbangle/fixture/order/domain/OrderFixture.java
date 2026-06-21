package com.bbangle.bbangle.fixture.order.domain;

import com.bbangle.bbangle.fixture.member.domain.MemberFixture;
import com.bbangle.bbangle.fixture.payment.domain.PaymentFixture;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.seller.domain.Seller;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.test.util.ReflectionTestUtils;

public final class OrderFixture {

    private OrderFixture() {
    }

    public static Order.OrderBuilder defaultOrder() {
        return Order.builder()
            .orderNumber("ORDER-2025-01-01-00001")
            .orderDate(LocalDateTime.of(2025, 1, 1, 10, 0, 0))
            .buyerName("홍길동")
            .buyerPhone("01012345678")
            .buyerSubPhone("02123456789")
            .deliveryFee(2500)
            .totalAmount(50000)
            .orderItems(new ArrayList<>());
    }

    public static Order createDefaultOrder() {
        return defaultOrder().build();
    }

    public static Order createOrderWithNumber(String orderNumber) {
        return defaultOrder()
            .orderNumber(orderNumber)
            .build();
    }

    public static Order createOrderWithAmount(Integer totalAmount) {
        return defaultOrder()
            .totalAmount(totalAmount)
            .build();
    }

    public static Order createOrderWithBuyerName(String buyerName) {
        return defaultOrder()
            .buyerName(buyerName)
            .build();
    }

    public static Order createOrderWithSeller(Seller seller) {
        return defaultOrder()
            .seller(seller)
            .build();
    }

    public static Order createWithMemberAndSeller(Long memberId, Seller seller, int totalAmount) {
        Order order = defaultOrder()
            .member(MemberFixture.createWithId(memberId))
            .seller(seller)
            .totalAmount(totalAmount)
            .build();
        Payment payment = PaymentFixture.createDefaultPayment(order);
        ReflectionTestUtils.setField(order, "payment", payment);
        return order;
    }

}
