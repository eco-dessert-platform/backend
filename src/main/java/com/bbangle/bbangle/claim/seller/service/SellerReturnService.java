package com.bbangle.bbangle.claim.seller.service;

import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus;
import com.bbangle.bbangle.claim.repository.ReturnRequestRepository;
import com.bbangle.bbangle.claim.seller.service.model.ReturnCreateCommand;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.OrderItemHistory;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ReturnContent;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ReturnCreateResponse;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SellerReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderItemHistoryRepository orderItemHistoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    public ReturnCreateResponse createReturn(ReturnCreateCommand command) {
        if (command.orderItemIds() == null || command.orderItemIds().isEmpty()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        List<Long> uniqueOrderItemIds = command.orderItemIds().stream()
            .distinct()
            .toList();

        int requestedCount = uniqueOrderItemIds.size();

        Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

        Long storeId = getStoreIdOrThrow(command.sellerId());

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdIn(
            order.getId(),
            uniqueOrderItemIds
        );

        Set<Long> foundIds = orderItems.stream()
            .map(OrderItem::getId)
            .collect(Collectors.toSet());

        if (!foundIds.isEmpty()) {
            assertOwnedOrderItems(order.getId(), new ArrayList<>(foundIds), storeId);
        }

        List<Long> notFoundIds = uniqueOrderItemIds.stream()
            .filter(id -> !foundIds.contains(id))
            .toList();

        List<Long> successOrderItemIds = new ArrayList<>();
        List<Long> failedOrderItemIds = new ArrayList<>(notFoundIds);

        for (OrderItem orderItem : orderItems) {
            if (orderItem.requestReturn()) {
                ReturnRequest returnRequest = ReturnRequest.builder()
                    .orderItem(orderItem)
                    .detailReason(command.reason())
                    .status(ReturnRequestRequestStatus.REQUESTED)
                    .build();
                returnRequestRepository.save(returnRequest);
                orderItemHistoryRepository.save(OrderItemHistory.create(orderItem));
                successOrderItemIds.add(orderItem.getId());
            } else {
                failedOrderItemIds.add(orderItem.getId());
            }
        }

        int successCount = successOrderItemIds.size();
        int failCount = failedOrderItemIds.size();

        SellerOrderResponse.Summary summary =
            SellerOrderResponse.Summary.of(requestedCount, successCount, failCount);

        ReturnContent content = ReturnContent.of(
            order.getId(),
            summary,
            successOrderItemIds,
            failedOrderItemIds
        );

        return ReturnCreateResponse.of(content);
    }

    private Long getStoreIdOrThrow(Long sellerId) {
        Long storeId = sellerRepository.findStoreIdBySellerId(sellerId);
        if (storeId == null) {
            throw new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND);
        }
        return storeId;
    }

    private void assertOwnedOrderItems(Long orderId, List<Long> orderItemIds, Long storeId) {
        long ownedCount = orderItemRepository.countOwnedOrderItems(orderId, orderItemIds, storeId);
        if (ownedCount != orderItemIds.size()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    @Transactional
    public void decision(List<Long> returnIds, Long sellerId, DecisionType decisionType, String reason) {
        Long validatedCount = returnRequestRepository.countReturnsBySeller(returnIds, sellerId);
        if (validatedCount == null || validatedCount != returnIds.size()) {
            throw new BbangleException(BbangleErrorCode.SELLER_CLAIM_MISMATCH);
        }

        List<ReturnRequest> returnRequests = returnRequestRepository.findAllById(returnIds);
        for (ReturnRequest returnRequest : returnRequests) {
            processDecision(returnRequest, decisionType, reason);
        }
    }

    private void processDecision(ReturnRequest returnRequest, DecisionType decisionType, String reason) {
        switch (decisionType) {
            case APPROVE -> {
                returnRequest.approve(reason);
                OrderItem orderItem = returnRequest.getOrderItem();
                orderItem.returnApprove();
                OrderItemHistory history = OrderItemHistory.create(orderItem);
                orderItemHistoryRepository.save(history);
            }
            case REJECT -> {
                returnRequest.reject(reason);
                OrderItem orderItem = returnRequest.getOrderItem();
                orderItem.returnReject();
                OrderItemHistory history = OrderItemHistory.create(orderItem);
                orderItemHistoryRepository.save(history);
            }
        }
    }

}
