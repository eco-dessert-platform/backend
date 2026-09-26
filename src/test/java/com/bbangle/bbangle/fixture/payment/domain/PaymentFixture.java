package com.bbangle.bbangle.fixture.payment.domain;

import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.payment.domain.PaymentMethod;
import com.bbangle.bbangle.payment.domain.PaymentStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public final class PaymentFixture {

    private static final int DEFAULT_TOTAL_AMOUNT = 50000;

    private PaymentFixture() {
    }

    /**
     * 주문에 연결된 기본 결제를 만든다.
     *
     * <p>연관관계 주인이 {@code Order.payment} 이므로 Order 쪽에 결제를 꽂아준다.
     * 영속성 컨텍스트에 붙어 있는 Order 라면 flush 시점에 {@code orders.payment_id} 가 기록된다.
     */
    public static Payment createDefaultPayment(Order order) {
        Payment payment = Payment.create(
            uniquePaymentNumber(),
            order.getMember(),
            order.getTotalAmount(),
            PaymentStatus.COMPLETED,
            PaymentMethod.CARD,
            LocalDateTime.now());

        ReflectionTestUtils.setField(order, "payment", payment);
        return payment;
    }

    public static Payment createDefaultPayment() {
        return createPaymentWithStatusAndMethod(PaymentStatus.COMPLETED, PaymentMethod.CARD);
    }

    public static Payment createPaymentWithStatus(PaymentStatus status) {
        return createPaymentWithStatusAndMethod(status, PaymentMethod.CARD);
    }

    public static Payment createPaymentWithMethod(PaymentMethod method) {
        return createPaymentWithStatusAndMethod(PaymentStatus.COMPLETED, method);
    }

    private static Payment createPaymentWithStatusAndMethod(PaymentStatus status, PaymentMethod method) {
        return Payment.create(
            uniquePaymentNumber(),
            null,
            DEFAULT_TOTAL_AMOUNT,
            status,
            method,
            status == PaymentStatus.COMPLETED ? LocalDateTime.now() : null);
    }

    private static String uniquePaymentNumber() {
        return "PAY-TEST-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

}
