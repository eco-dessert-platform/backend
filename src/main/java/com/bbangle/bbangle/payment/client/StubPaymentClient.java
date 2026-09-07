package com.bbangle.bbangle.payment.client;

import com.bbangle.bbangle.payment.client.dto.PaymentConfirmResult;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

// TossPayments 연동 전 스텁 — 항상 성공 반환. 실제 연동 시 TossPaymentClient로 교체 후 이 @Primary를 제거한다.
@Profile({"local", "test", "dev"})
@Primary
@Component
public class StubPaymentClient implements PaymentClient {

    @Override
    public PaymentConfirmResult confirm(String paymentKey, String orderNumber, Long amount) {
        return new PaymentConfirmResult(paymentKey, LocalDateTime.now());
    }
}
