package com.bbangle.bbangle.fixture.payment.domain;

import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.payment.domain.PaymentMethod;
import com.bbangle.bbangle.payment.domain.PaymentStatus;
import java.time.LocalDateTime;

public final class PaymentFixture {

    public static Payment createDefaultPayment() {
        return createPaymentWithStatusAndMethod(null, PaymentStatus.COMPLETED, PaymentMethod.CARD);
    }

    public static Payment createDefaultPayment(Order order) {
        return createPaymentWithStatusAndMethod(order, PaymentStatus.COMPLETED, PaymentMethod.CARD);
    }

    public static Payment createPaymentWithStatus(PaymentStatus status) {
        return createPaymentWithStatusAndMethod(null, status, PaymentMethod.CARD);
    }

    public static Payment createPaymentWithMethod(PaymentMethod method) {
        return createPaymentWithStatusAndMethod(null, PaymentStatus.COMPLETED, method);
    }

    private static Payment createPaymentWithStatusAndMethod(Order order, PaymentStatus status, PaymentMethod method) {
        return Payment.create(
            order,
            status,
            method,
            status == PaymentStatus.COMPLETED ? LocalDateTime.now() : null);
    }

}
