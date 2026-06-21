package com.bbangle.bbangle.claim.customer.service;

import com.bbangle.bbangle.claim.customer.controller.dto.CustomerReturnRequest;
import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus;
import com.bbangle.bbangle.claim.repository.ReturnRequestRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.OrderItemHistory;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CustomerReturnService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemHistoryRepository orderItemHistoryRepository;

    @Transactional
    public void requestReturn(Long orderId, Long customerId, CustomerReturnRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

        if (!order.getMember().getId().equals(customerId)) {
            throw new BbangleException(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }

        List<Long> uniqueOrderItemIds = request.orderItemIds().stream()
            .distinct()
            .toList();

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdIn(orderId, uniqueOrderItemIds);

        if (orderItems.size() != uniqueOrderItemIds.size()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        List<ReturnRequest> returnRequestsToSave = new ArrayList<>();
        List<OrderItemHistory> historiesToSave = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            if (!orderItem.requestReturn()) {
                throw new BbangleException(BbangleErrorCode.CLAIM_INVALID_STATUS);
            }
            returnRequestsToSave.add(ReturnRequest.builder()
                .orderItem(orderItem)
                .detailReason(request.reason())
                .status(ReturnRequestRequestStatus.REQUESTED)
                .build());
            historiesToSave.add(OrderItemHistory.create(orderItem));
        }

        returnRequestRepository.saveAll(returnRequestsToSave);
        orderItemHistoryRepository.saveAll(historiesToSave);
    }
}
