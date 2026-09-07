package com.bbangle.bbangle.payment.client;

import com.bbangle.bbangle.payment.client.dto.PaymentConfirmResult;

public interface PaymentClient {

    PaymentConfirmResult confirm(String paymentKey, String orderNumber, Long amount);
}
