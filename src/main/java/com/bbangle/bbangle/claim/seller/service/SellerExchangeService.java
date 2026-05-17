package com.bbangle.bbangle.claim.seller.service;

import com.bbangle.bbangle.claim.domain.ExchangeRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.domain.constant.ExchangeRequestStatus;
import com.bbangle.bbangle.claim.repository.ClaimRepository;
import com.bbangle.bbangle.claim.repository.ExchangeRequestRepository;
import com.bbangle.bbangle.claim.seller.service.model.ExchangeCreateCommand;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.OrderItemHistory;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ExchangeContent;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ExchangeCreateResponse;
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
public class SellerExchangeService {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final ClaimRepository claimRepository;
    private final OrderItemHistoryRepository orderItemHistoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    public ExchangeCreateResponse createExchange(ExchangeCreateCommand command) {
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
        List<ExchangeRequest> exchangeRequestsToSave = new ArrayList<>();
        List<OrderItemHistory> historiesToSave = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            if (orderItem.requestExchange()) {
                ExchangeRequest exchangeRequest = ExchangeRequest.builder()
                    .orderItem(orderItem)
                    .detailReason(command.reason())
                    .sellerComment(command.sellerComment())
                    .status(ExchangeRequestStatus.REQUESTED)
                    .build();
                exchangeRequestsToSave.add(exchangeRequest);
                historiesToSave.add(OrderItemHistory.create(orderItem));
                successOrderItemIds.add(orderItem.getId());
            } else {
                failedOrderItemIds.add(orderItem.getId());
            }
        }

        exchangeRequestRepository.saveAll(exchangeRequestsToSave);
        orderItemHistoryRepository.saveAll(historiesToSave);

        int successCount = successOrderItemIds.size();
        int failCount = failedOrderItemIds.size();

        SellerOrderResponse.Summary summary =
            SellerOrderResponse.Summary.of(requestedCount, successCount, failCount);

        ExchangeContent content = ExchangeContent.of(
            order.getId(),
            summary,
            successOrderItemIds,
            failedOrderItemIds
        );

        return ExchangeCreateResponse.of(content);
    }

    @Transactional
    public void processExchange(Long exchangeId, Long sellerId, DecisionType decisionType, String reason) {
        if (!claimRepository.existsClaimRequestBySeller(exchangeId, sellerId)) {
            throw new BbangleException(BbangleErrorCode.SELLER_CLAIM_MISMATCH);
        }

        ExchangeRequest exchangeRequest = findExchangeRequestWithLock(exchangeId);
        applyDecision(exchangeRequest, decisionType, reason);
    }

    private ExchangeRequest findExchangeRequestWithLock(Long exchangeId) {
        return exchangeRequestRepository.findWithLockById(exchangeId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.CLAIM_NOT_FOUND));
    }

    private void applyDecision(ExchangeRequest exchangeRequest, DecisionType decisionType, String reason) {
        OrderItem orderItem = exchangeRequest.getOrderItem();
        switch (decisionType) {
            case APPROVE -> {
                exchangeRequest.approve(reason);
                orderItem.exchangeApprove();
            }
            case REJECT -> {
                exchangeRequest.reject(reason);
                orderItem.exchangeReject();
            }
        }
        orderItemHistoryRepository.save(OrderItemHistory.create(orderItem));
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
}
