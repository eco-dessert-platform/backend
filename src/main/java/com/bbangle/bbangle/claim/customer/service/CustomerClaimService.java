package com.bbangle.bbangle.claim.customer.service;

import com.bbangle.bbangle.claim.customer.controller.dto.CustomerCancelRequest;
import com.bbangle.bbangle.claim.customer.controller.dto.CustomerExchangeRequest;
import com.bbangle.bbangle.claim.customer.controller.dto.CustomerReturnRequest;
import com.bbangle.bbangle.claim.domain.CancelRequest;
import com.bbangle.bbangle.claim.domain.ExchangeRequest;
import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.CancelRequestStatus;
import com.bbangle.bbangle.claim.domain.constant.ExchangeRequestStatus;
import com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus;
import com.bbangle.bbangle.claim.repository.CancelRequestRepository;
import com.bbangle.bbangle.claim.repository.ExchangeRequestRepository;
import com.bbangle.bbangle.claim.repository.ReturnRequestRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.OrderItemHistory;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CustomerClaimService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CancelRequestRepository cancelRequestRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemHistoryRepository orderItemHistoryRepository;

    @Transactional
    public void cancelOrder(Long orderId, Long customerId, CustomerCancelRequest request) {
        validateOrderOwnership(orderId, customerId);

        List<Long> uniqueOrderItemIds = request.orderItemIds().stream()
            .distinct()
            .toList();

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, uniqueOrderItemIds);

        if (orderItems.size() != uniqueOrderItemIds.size()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        // 취소 가능 상태 전면 검증 (All-or-Nothing 정책: 하나라도 불가하면 전체 실패)
        if (orderItems.stream().anyMatch(item -> !item.canRequestCancel())) {
            throw new BbangleException(BbangleErrorCode.CLAIM_INVALID_STATUS);
        }

        orderItems.forEach(OrderItem::requestCancel);

        List<CancelRequest> cancelRequestsToSave = orderItems.stream()
            .map(orderItem -> CancelRequest.builder()
                .orderItem(orderItem)
                .detailReason(request.reason())
                .status(CancelRequestStatus.REQUESTED)
                .build())
            .toList();

        List<OrderItemHistory> historiesToSave = orderItems.stream()
            .map(OrderItemHistory::create)
            .toList();

        cancelRequestRepository.saveAll(cancelRequestsToSave);
        orderItemHistoryRepository.saveAll(historiesToSave);
    }

    @Transactional
    public void requestReturn(Long orderId, Long customerId, CustomerReturnRequest request) {
        validateOrderOwnership(orderId, customerId);

        List<Long> uniqueOrderItemIds = request.orderItemIds().stream()
            .distinct()
            .toList();

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, uniqueOrderItemIds);

        if (orderItems.size() != uniqueOrderItemIds.size()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        // 반품 가능 상태 전면 검증 (All-or-Nothing 정책: 하나라도 불가하면 전체 실패)
        if (orderItems.stream().anyMatch(item -> !item.canRequestReturn())) {
            throw new BbangleException(BbangleErrorCode.CLAIM_INVALID_STATUS);
        }

        orderItems.forEach(OrderItem::requestReturn);

        List<ReturnRequest> returnRequestsToSave = orderItems.stream()
            .map(orderItem -> ReturnRequest.builder()
                .orderItem(orderItem)
                .detailReason(request.reason())
                .status(ReturnRequestRequestStatus.REQUESTED)
                .build())
            .toList();

        List<OrderItemHistory> historiesToSave = orderItems.stream()
            .map(OrderItemHistory::create)
            .toList();

        returnRequestRepository.saveAll(returnRequestsToSave);
        orderItemHistoryRepository.saveAll(historiesToSave);
    }

    @Transactional
    public void requestExchange(Long orderId, Long customerId, CustomerExchangeRequest request) {
        validateOrderOwnership(orderId, customerId);

        List<Long> uniqueOrderItemIds = request.orderItemIds().stream()
            .distinct()
            .toList();

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, uniqueOrderItemIds);

        if (orderItems.size() != uniqueOrderItemIds.size()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        // 교환 가능 상태 전면 검증 (All-or-Nothing 정책: 하나라도 불가하면 전체 실패)
        if (orderItems.stream().anyMatch(item -> !item.canRequestExchange())) {
            throw new BbangleException(BbangleErrorCode.CLAIM_INVALID_STATUS);
        }

        orderItems.forEach(OrderItem::requestExchange);

        List<ExchangeRequest> exchangeRequestsToSave = orderItems.stream()
            .map(orderItem -> ExchangeRequest.builder()
                .orderItem(orderItem)
                .detailReason(request.reason())
                .status(ExchangeRequestStatus.REQUESTED)
                .build())
            .toList();

        List<OrderItemHistory> historiesToSave = orderItems.stream()
            .map(OrderItemHistory::create)
            .toList();

        exchangeRequestRepository.saveAll(exchangeRequestsToSave);
        orderItemHistoryRepository.saveAll(historiesToSave);
    }

    private void validateOrderOwnership(Long orderId, Long customerId) {
        Order order = orderRepository.findByIdWithMember(orderId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

        if (!order.getMember().getId().equals(customerId)) {
            throw new BbangleException(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }
    }
}
