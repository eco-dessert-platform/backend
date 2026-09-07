package com.bbangle.bbangle.payment.customer.controller;

import com.bbangle.bbangle.payment.customer.dto.request.PaymentConfirmRequest;
import com.bbangle.bbangle.payment.customer.dto.response.PaymentConfirmResponse;
import com.bbangle.bbangle.payment.customer.service.CustomerPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer/payments")
@RequiredArgsConstructor
public class CustomerPaymentController {

    private final CustomerPaymentService customerPaymentService;

    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirm(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody PaymentConfirmRequest request
    ) {
        return ResponseEntity.ok(customerPaymentService.confirm(memberId, request));
    }
}
