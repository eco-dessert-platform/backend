package com.bbangle.bbangle.order.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.service.model.SellerOrderCommand.OrderConfirmCommand;
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

        Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

        Long storeId = sellerRepository.findStoreIdBySellerId(command.sellerId());
        if (storeId == null) {
            throw new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND);
        }

        long ownedCount = orderItemRepository.countOwnedOrderItems(
            order.getId(),
            command.orderItemIds(),
            storeId
        );

        if (ownedCount != command.orderItemIds().size()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdIn(
            order.getId(),
            command.orderItemIds()
        );

        List<Long> confirmedOrderItemIds = orderItems.stream()
            .filter(OrderItem::confirmOrder)
            .map(OrderItem::getId)
            .toList();

        return OrderConfirmResponse.of(order.getId(), confirmedOrderItemIds);
    }
}
