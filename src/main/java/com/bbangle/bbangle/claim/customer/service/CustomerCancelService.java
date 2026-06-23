package com.bbangle.bbangle.claim.customer.service;

import com.bbangle.bbangle.claim.customer.controller.dto.CustomerCancelRequest;
import com.bbangle.bbangle.claim.domain.CancelRequest;
import com.bbangle.bbangle.claim.domain.constant.CancelRequestStatus;
import com.bbangle.bbangle.claim.repository.CancelRequestRepository;
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
public class CustomerCancelService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CancelRequestRepository cancelRequestRepository;
    private final OrderItemHistoryRepository orderItemHistoryRepository;

    @Transactional
    public void requestCancel(Long orderId, Long customerId, CustomerCancelRequest request) {
        Order order = orderRepository.findByIdWithMember(orderId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

        if (!order.getMember().getId().equals(customerId)) {
            throw new BbangleException(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }

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

        // 전체 유효성 확인 후 상태 전이 실행
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
}
