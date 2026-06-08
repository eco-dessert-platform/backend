package com.bbangle.bbangle.order.customer.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.customer.controller.dto.response.OrderReceiptResponse;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.store.domain.Store;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderReceiptService {

    private static final Set<OrderStatus> EXCLUDED_STATUSES =
        EnumSet.of(OrderStatus.CANCEL_APPROVED, OrderStatus.RETURN_COMPLETED);

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public OrderReceiptResponse getReceipt(Long memberId, Long orderId) {
        Order order = orderRepository.findByIdWithFullAssociations(orderId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

        if (!order.getMember().getId().equals(memberId)) {
            throw new BbangleException(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }

        Payment payment = order.getPayment();
        // TODO: 카드번호 암호화 여부 결정 후 payment.getCardNumber() 복호화 로직 추가 필요
        Store store = order.getSeller().getStore();
        List<OrderItem> activeItems = order.getOrderItems().stream()
            .filter(item -> !EXCLUDED_STATUSES.contains(item.getOrderStatus()))
            .toList();

        return OrderReceiptResponse.from(payment, order.getOrderNumber(), activeItems, store);
    }
}
