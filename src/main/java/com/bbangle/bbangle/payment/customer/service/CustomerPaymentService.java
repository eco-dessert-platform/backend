package com.bbangle.bbangle.payment.customer.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.payment.client.PaymentClient;
import com.bbangle.bbangle.payment.client.dto.PaymentConfirmResult;
import com.bbangle.bbangle.payment.customer.dto.request.PaymentConfirmRequest;
import com.bbangle.bbangle.payment.customer.dto.response.PaymentConfirmResponse;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.payment.domain.PaymentStatus;
import com.bbangle.bbangle.payment.repository.PaymentRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerPaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentClient paymentClient;

    public PaymentConfirmResponse confirm(Long memberId, PaymentConfirmRequest request) {
        // 1. 대표 주문 조회 (프론트가 전달한 orderNumber)
        Order representativeOrder = orderRepository.findByOrderNumber(request.orderNumber())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

        // 2. 주문 소유자 검증
        if (!representativeOrder.getMember().getId().equals(memberId)) {
            throw new BbangleException(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }

        // 3. orderGroupId로 그룹 전체 Payment를 비관적 락으로 조회 (동시성 제어)
        String orderGroupId = representativeOrder.getOrderGroupId();
        List<Payment> payments = paymentRepository.findByOrderGroupIdWithLock(orderGroupId);

        if (payments.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.PAYMENT_NOT_FOUND);
        }

        // 4. 모든 Payment가 PENDING 상태인지 검증 (CANCELED, REFUNDED, COMPLETED 재승인 차단)
        payments.forEach(p -> {
            if (p.getPaymentStatus() != PaymentStatus.PENDING) {
                throw new BbangleException(BbangleErrorCode.PAYMENT_ALREADY_COMPLETED);
            }
        });

        // 5. 금액 검증: 그룹 전체 Order.totalAmount 합계 vs 요청 금액
        long totalGroupAmount = payments.stream()
            .mapToLong(p -> p.getOrder().getTotalAmount())
            .sum();
        if (!request.amount().equals(totalGroupAmount)) {
            throw new BbangleException(BbangleErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 6. 외부 결제 승인 1회 (PG는 단일 결제 세션)
        PaymentConfirmResult result = paymentClient.confirm(
            request.paymentKey(), request.orderNumber(), request.amount()
        );

        // 7. 그룹 전체 Payment와 OrderItem 상태를 COMPLETED / PAYMENT_COMPLETED로 전환
        for (Payment payment : payments) {
            payment.confirm(result.paymentKey(), result.approvedAt());
            List<OrderItem> orderItems = payment.getOrder().getOrderItems();
            orderItems.forEach(OrderItem::completePayment);
        }

        return new PaymentConfirmResponse(representativeOrder.getOrderNumber(), PaymentStatus.COMPLETED.name());
    }
}
