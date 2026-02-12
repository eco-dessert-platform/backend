package com.bbangle.bbangle.order.seller.service;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.repository.OrderDeliveryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderItemListResponse.OrderItemList;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderSearchResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.controller.model.PaymentInfo;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderConfirmCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.seller.repository.SellerRepository;
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

        Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

        Long storeId = sellerRepository.findStoreIdBySellerId(command.sellerId());
        if (storeId == null) {
            throw new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND);
        }

        long ownedCount = orderItemRepository.countOwnedOrderItems(
            order.getId(),
            uniqueOrderItemIds,
            storeId);

        if (ownedCount != uniqueOrderItemIds.size()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdIn(
            order.getId(),
            uniqueOrderItemIds);

        List<Long> confirmedOrderItemIds = orderItems.stream()
            .filter(OrderItem::confirmOrder)
            .map(OrderItem::getId)
            .toList();

        return OrderConfirmResponse.of(order.getId(), confirmedOrderItemIds);
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
                    log.warn("주문 데이터 무결성 문제 - OrderItem 누락: orderId={}, orderNumber={}",
                        order.getId(), order.getOrderNumber());
                    skippedCount++;
                    continue;
                }

                Payment payment = order.getPayment();
                if (payment == null) {
                    log.warn("주문 데이터 무결성 문제 - Payment 누락: orderId={}, orderNumber={}",
                        order.getId(), order.getOrderNumber());
                    skippedCount++;
                    continue;
                }
                PaymentInfo paymentInfo = PaymentInfo.of(payment.getPaymentStatus().getDescription(),
                    payment.getPaymentMethod().getDescription());

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
