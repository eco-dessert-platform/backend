package com.bbangle.bbangle.order.seller.service;

import com.bbangle.bbangle.delivery.domain.Receiver;
import com.bbangle.bbangle.delivery.domain.Sender;
import com.bbangle.bbangle.delivery.domain.Shipping;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.repository.OrderDeliveryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentContent;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentRegisterResponse;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderConfirmCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.ShipmentRegisterCommand;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerOrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderDeliveryRepository orderDeliveryRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    public OrderConfirmResponse confirmOrder(OrderConfirmCommand command) {
        List<Long> uniqueOrderItemIds = validateAndDeduplicateIds(command.orderItemIds());
        int requestedCount = uniqueOrderItemIds.size();

        Order order = getOrderOrThrow(command.orderId());
        Seller seller = getSellerWithStoreOrThrow(command.sellerId());

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdIn(
            order.getId(), uniqueOrderItemIds
        );
        List<Long> notFoundIds = computeNotFoundIds(uniqueOrderItemIds, orderItems);
        List<Long> foundIds = orderItems.stream().map(OrderItem::getId).toList();
        if (!foundIds.isEmpty()) {
            assertOwnedOrderItems(order.getId(), foundIds, seller.getStore().getId());
        }

        List<Long> confirmedIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>(notFoundIds);

        for (OrderItem orderItem : orderItems) {
            if (orderItem.confirmOrder()) {
                confirmedIds.add(orderItem.getId());
            } else {
                failedIds.add(orderItem.getId());
            }
        }

        SellerOrderResponse.Summary summary = SellerOrderResponse.Summary.of(
            requestedCount, confirmedIds.size(), failedIds.size()
        );
        SellerOrderResponse.Content content = SellerOrderResponse.Content.of(
            order.getId(), summary, confirmedIds, failedIds
        );
        return OrderConfirmResponse.of(content);
    }

    @Transactional
    public ShipmentRegisterResponse registerShipment(ShipmentRegisterCommand command) {
        List<Long> uniqueOrderItemIds = validateAndDeduplicateIds(command.orderItemIds());
        int requestedCount = uniqueOrderItemIds.size();

        Order order = getOrderOrThrow(command.orderId());
        Seller seller = getSellerWithStoreOrThrow(command.sellerId());

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdInWithOrder(
            order.getId(), uniqueOrderItemIds
        );
        List<Long> notFoundIds = computeNotFoundIds(uniqueOrderItemIds, orderItems);
        List<Long> foundIds = orderItems.stream().map(OrderItem::getId).toList();
        if (!foundIds.isEmpty()) {
            assertOwnedOrderItems(order.getId(), foundIds, seller.getStore().getId());
        }
        Map<Long, OrderDelivery> deliveryMap = loadDeliveryMap(uniqueOrderItemIds);

        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>(notFoundIds);
        LocalDateTime shippedAt = null;

        for (OrderItem orderItem : orderItems) {
            try {
                OrderDelivery delivery = getOrCreateDelivery(deliveryMap, orderItem, seller);
                delivery.registerShipment(command.courierName(), command.trackingNumber());
                orderItem.shipOrder();

                successIds.add(orderItem.getId());
                shippedAt = delivery.getShipping().getShippedAt();
            } catch (BbangleException e) {
                log.warn("운송장 등록 실패 - orderId: {}, orderItemId: {}, sellerId: {}, reason: {}",
                    command.orderId(), orderItem.getId(), command.sellerId(), e.getMessage());
                failedIds.add(orderItem.getId());
            }
        }

        boolean hasSuccess = !successIds.isEmpty();
        SellerOrderResponse.Summary summary = SellerOrderResponse.Summary.of(
            requestedCount, successIds.size(), failedIds.size()
        );
        ShipmentContent content = ShipmentContent.of(
            order.getId(), summary,
            successIds, failedIds,
            hasSuccess ? command.courierName() : null,
            hasSuccess ? command.trackingNumber() : null,
            shippedAt
        );
        return ShipmentRegisterResponse.of(content);
    }

    private List<Long> validateAndDeduplicateIds(List<Long> orderItemIds) {
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }
        return orderItemIds.stream().distinct().toList();
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));
    }

    private Seller getSellerWithStoreOrThrow(Long sellerId) {
        return sellerRepository.findByIdWithStore(sellerId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND));
    }

    private void assertOwnedOrderItems(Long orderId, List<Long> orderItemIds, Long storeId) {
        long ownedCount = orderItemRepository.countOwnedOrderItems(orderId, orderItemIds, storeId);
        if (ownedCount != orderItemIds.size()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    private List<Long> computeNotFoundIds(List<Long> requestedIds, List<OrderItem> foundItems) {
        Set<Long> foundIds = foundItems.stream()
            .map(OrderItem::getId)
            .collect(Collectors.toSet());
        return requestedIds.stream()
            .filter(id -> !foundIds.contains(id))
            .toList();
    }

    private Map<Long, OrderDelivery> loadDeliveryMap(List<Long> orderItemIds) {
        return orderDeliveryRepository.findByOrderItemIdIn(orderItemIds).stream()
            .collect(Collectors.toMap(
                od -> od.getOrderItem().getId(),
                Function.identity(),
                (existing, duplicate) -> existing
            ));
    }

    private OrderDelivery getOrCreateDelivery(Map<Long, OrderDelivery> deliveryMap,
                                               OrderItem orderItem, Seller seller) {
        OrderDelivery delivery = deliveryMap.get(orderItem.getId());
        if (delivery != null) {
            return delivery;
        }
        OrderDelivery newDelivery = createOrderDelivery(orderItem, seller);
        orderDeliveryRepository.save(newDelivery);
        return newDelivery;
    }

    private OrderDelivery createOrderDelivery(OrderItem orderItem, Seller seller) {
        Order order = orderItem.getOrder();
        Store store = seller.getStore();

        Sender sender = Sender.of(
            store.getName(),
            store.getPhoneNumberVO() != null ? store.getPhoneNumberVO().getPhoneNumber() : null,
            store.getOriginAddressLine(),
            store.getOriginAddressDetail(),
            null
        );

        Receiver receiver = Receiver.of(
            order.getBuyerName(),
            order.getBuyerPhone(),
            order.getBuyerSubPhone(),
            null,
            null,
            null
        );

        return OrderDelivery.create(
            sender,
            receiver,
            Shipping.empty(),
            OrderDeliveryStatus.PREPARING,
            orderItem
        );
    }
}
