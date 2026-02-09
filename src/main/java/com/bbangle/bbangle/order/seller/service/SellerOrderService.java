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
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentRegisterResponse;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderConfirmCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.ShipmentRegisterCommand;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

        Long storeId = getStoreIdOrThrow(command.sellerId());
        assertOwnedOrderItems(order.getId(), uniqueOrderItemIds, storeId);

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdIn(
            order.getId(),
            uniqueOrderItemIds
        );

        List<Long> confirmedOrderItemIds = orderItems.stream()
            .filter(OrderItem::confirmOrder)
            .map(OrderItem::getId)
            .toList();

        return OrderConfirmResponse.of(order.getId(), confirmedOrderItemIds);
    }

    @Transactional
    public ShipmentRegisterResponse registerShipment(ShipmentRegisterCommand command) {
        // 1. orderItemIds null/empty 체크
        if (command.orderItemIds() == null || command.orderItemIds().isEmpty()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        // 2. 중복 제거
        List<Long> uniqueOrderItemIds = command.orderItemIds().stream()
            .distinct()
            .toList();

        // 3. 주문 조회
        Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.ORDER_NOT_FOUND));

        // 4. 판매자 + 스토어 fetch join 조회 (createOrderDelivery에서 필요)
        Seller seller = sellerRepository.findByIdWithStore(command.sellerId())
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND));

        // 5. 소유자 검증
        Long storeId = seller.getStore().getId();
        assertOwnedOrderItems(order.getId(), uniqueOrderItemIds, storeId);

        // 6. OrderItem 목록 조회 (Order fetch join으로 LAZY 로딩 방지)
        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdInWithOrder(
            order.getId(),
            uniqueOrderItemIds
        );

        // 7. 기존 OrderDelivery 일괄 조회 (N+1 방지)
        List<OrderDelivery> existingDeliveries = orderDeliveryRepository.findByOrderItemIdIn(uniqueOrderItemIds);
        Map<Long, OrderDelivery> deliveryMap = existingDeliveries.stream()
            .collect(Collectors.toMap(od -> od.getOrderItem().getId(), Function.identity()));

        // 8. 각 orderItem에 대해 운송장 등록 처리
        List<Long> successOrderItemIds = new ArrayList<>();
        List<Long> failedOrderItemIds = new ArrayList<>();
        LocalDateTime shippedAt = null;

        for (OrderItem orderItem : orderItems) {
            try {
                // OrderDelivery 조회 또는 생성
                OrderDelivery orderDelivery = deliveryMap.get(orderItem.getId());
                if (orderDelivery == null) {
                    orderDelivery = createOrderDelivery(orderItem, seller);
                    orderDeliveryRepository.save(orderDelivery);
                }

                // 운송장 정보 등록
                orderDelivery.registerShipment(command.courierName(), command.trackingNumber());

                // OrderItem 상태 변경
                orderItem.shipOrder();

                successOrderItemIds.add(orderItem.getId());
                shippedAt = orderDelivery.getShipping().getShippedAt();
            } catch (Exception e) {
                failedOrderItemIds.add(orderItem.getId());
            }
        }

        // 9. 응답 생성
        return ShipmentRegisterResponse.of(
            order.getId(),
            successOrderItemIds,
            failedOrderItemIds,
            command.courierName(),
            command.trackingNumber(),
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

        Sender sender = Sender.of(
            seller.getStore().getName(),
            seller.getPhoneNumberVO() != null ? seller.getPhoneNumberVO().getPhoneNumber() : null,
            seller.getOriginAddressLine(),
            seller.getOriginAddressDetail(),
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
}
