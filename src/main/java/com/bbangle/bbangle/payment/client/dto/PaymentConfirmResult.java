package com.bbangle.bbangle.payment.client.dto;

import java.time.LocalDateTime;

public record PaymentConfirmResult(String paymentKey, LocalDateTime approvedAt) {

}
