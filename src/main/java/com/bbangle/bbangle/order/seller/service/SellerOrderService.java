package com.bbangle.bbangle.order.seller.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderConfirmCommand;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SellerOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    public OrderConfirmResponse confirmOrder(OrderConfirmCommand command) {

        if (command.orderItemIds() == null || command.orderItemIds().isEmpty()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        List<Long> uniqueOrderItemIds = command.orderItemIds().stream()
            .distinct()
            .toList();

        int requestedCount = uniqueOrderItemIds.size();

        Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

        Long storeId = sellerRepository.findStoreIdBySellerId(command.sellerId());
        if (storeId == null) {
            throw new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND);
        }

        long ownedCount = orderItemRepository.countOwnedOrderItems(
            order.getId(),
            uniqueOrderItemIds,
            storeId
        );

        if (ownedCount != uniqueOrderItemIds.size()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdIn(
            order.getId(),
            uniqueOrderItemIds
        );

        java.util.Set<Long> foundIds = orderItems.stream()
            .map(OrderItem::getId)
            .collect(java.util.stream.Collectors.toSet());

        List<Long> notFoundIds = uniqueOrderItemIds.stream()
            .filter(id -> !foundIds.contains(id))
            .toList();

        List<Long> confirmedOrderItemIds = new java.util.ArrayList<>();
        List<Long> failedOrderItemIds = new java.util.ArrayList<>(notFoundIds);

        for (OrderItem orderItem : orderItems) {
            if (orderItem.confirmOrder()) {
                confirmedOrderItemIds.add(orderItem.getId());
            } else {
                failedOrderItemIds.add(orderItem.getId());
            }
        }

        int successCount = confirmedOrderItemIds.size();
        int failCount = failedOrderItemIds.size();

        SellerOrderResponse.Summary summary =
            SellerOrderResponse.Summary.of(requestedCount, successCount, failCount);

        SellerOrderResponse.Content content =
            SellerOrderResponse.Content.of(
                order.getId(),
                summary,
                confirmedOrderItemIds,
                failedOrderItemIds
            );

        return OrderConfirmResponse.of(content);
    }
}
