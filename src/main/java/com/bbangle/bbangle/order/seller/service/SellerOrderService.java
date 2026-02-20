package com.bbangle.bbangle.order.seller.service;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
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
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderItemListResponse.OrderItemList;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderSearchResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentRegisterResponse;
import com.bbangle.bbangle.order.seller.controller.model.PaymentInfo;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderConfirmCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.ShipmentRegisterCommand;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        assertOwnedOrderItems(order.getId(), uniqueOrderItemIds, storeId);

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

    @Transactional
    public ShipmentRegisterResponse registerShipment(ShipmentRegisterCommand command) {
        if (command.orderItemIds() == null || command.orderItemIds().isEmpty()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        List<Long> uniqueOrderItemIds = command.orderItemIds().stream()
            .distinct()
            .toList();

        Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

        Seller seller = sellerRepository.findByIdWithStore(command.sellerId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND));

        Long storeId = seller.getStore().getId();
        assertOwnedOrderItems(order.getId(), uniqueOrderItemIds, storeId);

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdInWithOrder(
            order.getId(),
            uniqueOrderItemIds
        );

        List<OrderDelivery> existingDeliveries = orderDeliveryRepository.findByOrderItemIdIn(uniqueOrderItemIds);
        Map<Long, OrderDelivery> deliveryMap = existingDeliveries.stream()
            .collect(Collectors.toMap(od -> od.getOrderItem().getId(), Function.identity(),
                (existing, duplicate) -> existing));

        List<Long> successOrderItemIds = new ArrayList<>();
        List<Long> failedOrderItemIds = new ArrayList<>();
        LocalDateTime shippedAt = null;

        for (OrderItem orderItem : orderItems) {
            try {
                OrderDelivery orderDelivery = deliveryMap.get(orderItem.getId());
                if (orderDelivery == null) {
                    orderDelivery = createOrderDelivery(orderItem, seller);
                    orderDeliveryRepository.save(orderDelivery);
                }

                orderDelivery.registerShipment(command.courierName(), command.trackingNumber());

                orderItem.shipOrder();

                successOrderItemIds.add(orderItem.getId());
                shippedAt = orderDelivery.getShipping().getShippedAt();
            } catch (BbangleException e) {
                log.warn("운송장 등록 실패 - orderId: {}, orderItemId: {}, sellerId: {}, reason: {}",
                    command.orderId(), orderItem.getId(), command.sellerId(), e.getMessage());
                failedOrderItemIds.add(orderItem.getId());
            }
        }

        boolean hasSuccess = !successOrderItemIds.isEmpty();

        return ShipmentRegisterResponse.of(
            order.getId(),
            successOrderItemIds,
            failedOrderItemIds,
            hasSuccess ? command.courierName() : null,
            hasSuccess ? command.trackingNumber() : null,
            shippedAt
        );
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

        Shipping shipping = Shipping.empty();

        return OrderDelivery.create(
            sender,
            receiver,
            shipping,
            OrderDeliveryStatus.PREPARING,
            orderItem
        );
    }

    @Transactional(readOnly = true)
    public BbanglePageResponse<OrderSearchResponse> orderSearch(OrderSearchCommand command) {
        BbanglePageResponse<Order> orderPage = orderRepository.searchOrderList(command);

        Map<Long, OrderDelivery> latestDeliveryMap = fetchLatestDeliveries(orderPage.content());

        List<OrderSearchResponse> responses = new ArrayList<>();
        int skippedCount = 0;

        for (Order order : orderPage.content()) {
            try {
                List<OrderItemList> orderItemList = getOrderItemLists(order, latestDeliveryMap);

                if (orderItemList.isEmpty()) {
                    log.info("주문에 OrderItem 없음 (상품 정보 누락 상태): orderId={}, orderNumber={}",
                        order.getId(), order.getOrderNumber());
                }

                Payment payment = order.getPayment();
                PaymentInfo paymentInfo = null;
                if (payment != null) {
                    paymentInfo = PaymentInfo.of(payment.getPaymentStatus().getDescription(),
                        payment.getPaymentMethod().getDescription());
                } else {
                    log.info("결제 정보 없음 (결제 대기 상태): orderId={}, orderNumber={}",
                        order.getId(), order.getOrderNumber());
                }

                OrderDelivery firstDelivery = order.getOrderItems().stream()
                    .map(item -> latestDeliveryMap.get(item.getId()))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);

                OrderSearchResponse response = OrderSearchResponse.from(
                    order, orderItemList, paymentInfo, firstDelivery);
                responses.add(response);

            } catch (Exception e) {
                log.error("주문 조회 중 예외 발생 - orderId={}, orderNumber={}",
                    order.getId(), order.getOrderNumber(), e);
                skippedCount++;
            }
        }

        if (skippedCount > 0) {
            log.warn("주문 조회에서 {}건의 주문을 스킵했습니다. sellerId={}",
                skippedCount, command.sellerId());
        }

        return new BbanglePageResponse<>(
            responses,
            orderPage.page(),
            orderPage.size(),
            orderPage.totalPages(),
            orderPage.totalElements());
    }

    private Map<Long, OrderDelivery> fetchLatestDeliveries(List<Order> orders) {
        List<Long> orderItemIds = orders.stream()
            .flatMap(order -> order.getOrderItems().stream())
            .map(OrderItem::getId)
            .toList();

        if (orderItemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return orderDeliveryRepository.findLatestByOrderItemIds(orderItemIds).stream()
            .collect(Collectors.toMap(
                delivery -> delivery.getOrderItem().getId(),
                Function.identity(),
                (existing, replacement) -> existing
            ));
    }

    private List<OrderItemList> getOrderItemLists(Order order, Map<Long, OrderDelivery> deliveryMap) {
        return order.getOrderItems().stream()
            .map(item -> OrderItemList.from(
                order.getOrderNumber(),
                item,
                deliveryMap.get(item.getId())))
            .toList();
    }
}
