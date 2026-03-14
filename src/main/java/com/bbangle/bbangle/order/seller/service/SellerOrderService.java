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
import com.bbangle.bbangle.order.domain.model.CompletedOrderStatus;
import com.bbangle.bbangle.order.domain.model.DayOfWeek;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderDeliveryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderItemListResponse.OrderItemList;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderItemDetailResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderItemDetailResponse.BuyerInfo;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderItemDetailResponse.OrderInfo;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderItemDetailResponse.ShippingInfo;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderSearchPageResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderSearchResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse.OrderStatusCounts;
import com.bbangle.bbangle.order.seller.controller.dto.response.CompletedOrderResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.CompletedOrderResponse.OrderSummary;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentContent;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentModifyResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentRegisterResponse;
import com.bbangle.bbangle.order.seller.controller.model.PaymentInfo;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderConfirmCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.ShipmentModifyCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.CompletedOrderSearchCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.ShipmentRegisterCommand;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    // ========================================================================================
    // [예주] 발주 확인 / 운송장 관리
    // ========================================================================================

    /**
     * 발주 확인 처리 (부분 성공 정책 적용)
     * 요청된 orderItemIds 중 일부만 확정 가능한 경우에도 가능한 항목은 계속 처리합니다.
     * 실패 항목(DB에 없거나 상태 전환 불가)은 failedIds에 수집하여 응답으로 반환합니다.
     * 일부 실패가 발생해도 성공한 항목은 롤백하지 않습니다.
     */
    @Transactional
    public OrderConfirmResponse confirmOrder(OrderConfirmCommand command) {
        List<Long> uniqueOrderItemIds = validateAndDeduplicateIds(command.orderItemIds());
        int requestedCount = uniqueOrderItemIds.size();

        Order order = getOrderOrThrow(command.orderId());
        Seller seller = getSellerWithStoreOrThrow(command.sellerId());

        List<OrderItem> orderItems = orderItemRepository.findByOrderIdAndIdIn(
            order.getId(), uniqueOrderItemIds
        );
        // DB에서 조회되지 않은 ID는 즉시 실패 목록에 추가
        List<Long> notFoundIds = computeNotFoundIds(uniqueOrderItemIds, orderItems);
        List<Long> foundIds = orderItems.stream().map(OrderItem::getId).toList();
        // 조회된 항목이 이 판매자의 스토어 소속인지 소유권 검증
        if (!foundIds.isEmpty()) {
            assertOwnedOrderItems(order.getId(), foundIds, seller.getStore().getId());
        }

        List<Long> confirmedIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>(notFoundIds);

        for (OrderItem orderItem : orderItems) {
            // confirmOrder()는 현재 상태가 발주확인 가능한 상태(결제완료)일 때만 true를 반환
            if (orderItem.confirmOrder()) {
                confirmedIds.add(orderItem.getId());
            } else {
                // 이미 확인 처리됐거나 전환 불가능한 상태이면 실패 처리
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

    /**
     * 신규 운송장 등록 (부분 성공 정책 적용)
     * 각 OrderItem에 대해 기존 OrderDelivery를 재사용하거나 새로 생성하여 택배사·운송장 번호를 등록합니다.
     * - 기존 OrderDelivery가 있으면 재사용, 없으면 판매자·주문 정보로 새로 생성 후 저장
     * - loadDeliveryMap으로 배송 정보를 일괄 사전 로드하여 반복문 내 N+1 쿼리를 방지합니다
     * 각 항목 처리 중 BbangleException이 발생하면 해당 항목만 실패 처리하고 나머지는 계속 진행합니다.
     */
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
        // orderItemId → OrderDelivery 매핑을 미리 로드 (for문 내 개별 조회로 인한 N+1 방지)
        Map<Long, OrderDelivery> deliveryMap = loadDeliveryMap(uniqueOrderItemIds);

        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>(notFoundIds);
        LocalDateTime shippedAt = null;

        for (OrderItem orderItem : orderItems) {
            try {
                // 기존 OrderDelivery가 없으면 판매자·주문 정보로 신규 생성 후 저장
                OrderDelivery delivery = getOrCreateDelivery(deliveryMap, orderItem, seller);
                // 택배사·운송장 번호를 설정하고 출고 시각을 기록
                delivery.registerShipment(command.courierName(), command.trackingNumber());
                // OrderItem 상태를 배송중(SHIPPED)으로 전환
                orderItem.shipOrder();

                successIds.add(orderItem.getId());
                shippedAt = delivery.getShipping().getShippedAt();
            } catch (BbangleException e) {
                // 개별 항목 실패는 로그만 남기고 다음 항목 처리를 계속 (부분 성공 정책)
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

    /**
     * 기존 운송장 수정 (registerShipment와의 차이점)
     * registerShipment는 OrderDelivery가 없으면 신규 생성하지만,
     * modifyShipment는 반드시 기존 OrderDelivery가 존재해야 합니다.
     * OrderDelivery가 없는 항목은 DELIVERY_NOT_FOUND 예외로 failedIds에 수집됩니다.
     */
    @Transactional
    public ShipmentModifyResponse modifyShipment(ShipmentModifyCommand command) {
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
        Map<Long, OrderDelivery> deliveryMap = loadDeliveryMap(foundIds);

        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>(notFoundIds);
        LocalDateTime shippedAt = null;

        for (OrderItem orderItem : orderItems) {
            try {
                OrderDelivery delivery = deliveryMap.get(orderItem.getId());
                if (delivery == null) {
                    throw new BbangleException(BbangleErrorCode.DELIVERY_NOT_FOUND);
                }
                delivery.modifyShipment(command.courierName(), command.trackingNumber());

                successIds.add(orderItem.getId());
                shippedAt = delivery.getShipping().getShippedAt();
            } catch (BbangleException e) {
                log.warn("운송장 수정 실패 - orderId: {}, orderItemId: {}, sellerId: {}, reason: {}",
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
        return ShipmentModifyResponse.of(content);
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

    // ========================================================================================
    // [Joon Gyu] 주문 조회
    // ========================================================================================

    /**
     * 주문 항목 상세 조회 (배송 정보 포함)
     * OrderItem과 Order는 fetchJoin으로 함께 조회하고, 배송 정보(OrderDelivery)는 별도로 조회합니다.
     * 배송 정보를 분리 조회하는 이유: 운송장 등록 전에는 OrderDelivery가 없을 수도 있어 optional한 관계이며,
     * orderItem.orderDeliveries fetchJoin 시 카테시안 곱 문제가 발생할 수 있기 때문입니다.
     *
     * Fallback 패턴: 수취인 이름·전화번호는 OrderDelivery.Receiver가 있으면 우선 사용하고,
     * 없으면 주문자(Order.buyerName/buyerPhone) 정보로 대체합니다.
     */
    @Transactional(readOnly = true)
    public List<OrderItemDetailResponse> searchOrderItemDetails(List<Long> orderItemIds, Long sellerId) {
        if (orderItemIds == null || orderItemIds.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        List<Long> uniqueOrderItemIds = orderItemIds.stream()
            .distinct()
            .toList();

        Long storeId = getStoreIdOrThrow(sellerId);
        assertOwnedOrderItemsByStoreId(uniqueOrderItemIds, storeId);

        List<OrderItem> orderItems = orderItemRepository.findWithOrderAndProductByIdIn(uniqueOrderItemIds);

        Map<Long, OrderDelivery> latestDeliveryByOrderItemId =
            orderDeliveryRepository.findLatestByOrderItemIds(uniqueOrderItemIds).stream()
                .collect(Collectors.toMap(
                    od -> od.getOrderItem().getId(),
                    Function.identity(),
                    (existing, replacement) -> existing
                ));

        return orderItems.stream()
            .map(oi -> {
                Order order = oi.getOrder();
                OrderDelivery delivery = latestDeliveryByOrderItemId.get(oi.getId());

                String orderDate = order.getOrderDate() != null
                    ? order.getOrderDate().toLocalDate().toString()
                    : null;
                String orderStatusLabel = oi.getOrderStatus() != null
                    ? oi.getOrderStatus().getDescription()
                    : null;
                OrderInfo orderInfo = new OrderInfo(orderDate, orderStatusLabel);

                String recipientName = (delivery != null && delivery.getReceiver() != null)
                    ? delivery.getReceiver().getRecipientName()
                    : order.getBuyerName();
                String phone1 = (delivery != null && delivery.getReceiver() != null)
                    ? delivery.getReceiver().getRecipientPhone1()
                    : order.getBuyerPhone();
                String phone2 = (delivery != null && delivery.getReceiver() != null)
                    ? delivery.getReceiver().getRecipientPhone2()
                    : order.getBuyerSubPhone();
                BuyerInfo buyerInfo = new BuyerInfo(recipientName, order.getBuyerName(), phone1, phone2);

                String statusLabel = null;
                String courierCompany = null;
                String trackingNumber = null;
                String address = null;
                String memo = null;
                Long shippingFee = order.getDeliveryFee() != null ? order.getDeliveryFee().longValue() : null;

                if (delivery != null) {
                    statusLabel = delivery.getStatus() != null
                        ? delivery.getStatus().getDescription()
                        : null;
                    Shipping shipping = delivery.getShipping();
                    if (shipping != null) {
                        courierCompany = shipping.getCourierName();
                        trackingNumber = shipping.getTrackingNumber();
                        memo = shipping.getDeliveryMemo();
                    }
                    Receiver receiver = delivery.getReceiver();
                    if (receiver != null) {
                        String base = receiver.getRecipientAddress();
                        String detail = receiver.getRecipientAddressDetail();
                        if (base != null && detail != null) {
                            address = base + " " + detail;
                        } else if (base != null) {
                            address = base;
                        }
                    }
                }
                ShippingInfo shippingInfo = new ShippingInfo(
                    statusLabel, courierCompany, trackingNumber, shippingFee, address, memo);

                OrderItemDetailResponse.OrderItem orderItemDto = new OrderItemDetailResponse.OrderItem(
                    oi.getProduct().getBoard().getTitle(),
                    oi.getProduct().getTitle(),
                    oi.getQuantity(),
                    oi.getUnitPrice() != null ? oi.getUnitPrice().longValue() : null,
                    oi.getTotalPrice() != null ? oi.getTotalPrice().longValue() : null
                );

                return new OrderItemDetailResponse(
                    order.getOrderNumber(), orderInfo, buyerInfo, shippingInfo, orderItemDto);
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public OrderSearchPageResponse orderSearch(OrderSearchCommand command) {
        BbanglePageResponse<Order> orderPage = orderRepository.searchOrderList(command);
        Map<OrderStatus, Long> statusCountMap = orderRepository.countByOrderStatus(command);

        // orderItems가 fetchJoin으로 초기화되어 있어야 합니다.
        Map<Long, OrderDelivery> latestDeliveryMap = fetchLatestDeliveries(orderPage.content());

        List<OrderSearchResponse> responses = new ArrayList<>();

        for (Order order : orderPage.content()) {
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
                log.debug("결제 정보 없음 (결제 대기 상태): orderId={}, orderNumber={}",
                    order.getId(), order.getOrderNumber());
            }

            // ID 기준 정렬로 결정적(deterministic) 선택 보장
            OrderDelivery firstDelivery = order.getOrderItems().stream()
                .sorted(Comparator.comparingLong(OrderItem::getId))
                .map(item -> latestDeliveryMap.get(item.getId()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

            OrderSearchResponse response = OrderSearchResponse.from(
                order, orderItemList, paymentInfo, firstDelivery);
            responses.add(response);
        }

        BbanglePageResponse<OrderSearchResponse> ordersPage = new BbanglePageResponse<>(
            responses,
            orderPage.page(),
            orderPage.size(),
            orderPage.totalPages(),
            orderPage.totalElements());

        OrderStatusCounts statusCounts = buildStatusCounts(statusCountMap);

        return new OrderSearchPageResponse(ordersPage, statusCounts);
    }

    private void assertOwnedOrderItemsByStoreId(List<Long> orderItemIds, Long storeId) {
        long ownedCount = orderItemRepository.countOwnedOrderItemsByStoreId(orderItemIds, storeId);
        if (ownedCount != orderItemIds.size()) {
            throw new BbangleException(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }
    }

    private Long getStoreIdOrThrow(Long sellerId) {
        Long storeId = sellerRepository.findStoreIdBySellerId(sellerId);
        if (storeId == null) {
            throw new BbangleException(BbangleErrorCode.SELLER_NOT_FOUND);
        }
        return storeId;
    }

    /**
     * DB 조회 결과인 OrderStatus별 카운트 Map을 UI 표시용 7개 탭 카운트로 변환합니다.
     * OrderStatus는 세부 상태(예: CANCEL_REQUESTED, CANCELLED)를 포함하는 enum이므로,
     * 탭별로 관련 상태들의 카운트를 합산합니다 (예: CANCELLED_GROUP = {CANCEL_REQUESTED, CANCELLED}).
     */
    private OrderStatusCounts buildStatusCounts(Map<OrderStatus, Long> countMap) {
        return OrderStatusCounts.of(
            sumCounts(countMap, OrderStatus.PAYMENT_COMPLETED_GROUP),
            sumCounts(countMap, OrderStatus.ORDER_CONFIRMED_GROUP),
            sumCounts(countMap, OrderStatus.SHIPPED_GROUP),
            sumCounts(countMap, OrderStatus.DELIVERY_COMPLETED_GROUP),
            sumCounts(countMap, OrderStatus.CANCELLED_GROUP),
            sumCounts(countMap, OrderStatus.RETURNED_GROUP),
            sumCounts(countMap, OrderStatus.EXCHANGED_GROUP)
        );
    }

    private long sumCounts(Map<OrderStatus, Long> countMap, Set<OrderStatus> statuses) {
        return statuses.stream()
            .mapToLong(status -> countMap.getOrDefault(status, 0L))
            .sum();
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

    @Transactional(readOnly = true)
    public CompletedOrderResponse.CompletedOrderPageResponse getCompletedOrders(CompletedOrderSearchCommand command) {
        BbanglePageResponse<Order> orderPage = orderRepository.searchCompletedOrderList(command);
        Map<OrderStatus, Long> statusCountMap = orderRepository.countByCompletedOrderStatus(command);

        Map<Long, OrderDelivery> latestDeliveryMap = fetchLatestDeliveries(orderPage.content());

        List<OrderSummary> orderSummaries = orderPage.content().stream()
            .map(order -> buildOrderSummary(order, latestDeliveryMap))
            .toList();

        BbanglePageResponse<OrderSummary> pageResponse = new BbanglePageResponse<>(
            orderSummaries,
            orderPage.page(),
            orderPage.size(),
            orderPage.totalPages(),
            orderPage.totalElements()
        );

        long purchasedCount = sumCounts(statusCountMap, Set.of(OrderStatus.PURCHASE_CONFIRMED));
        long canceledCount = sumCounts(statusCountMap, OrderStatus.CANCELLED_GROUP);
        long returnedCount = sumCounts(statusCountMap, OrderStatus.RETURNED_GROUP);
        long exchangedCount = sumCounts(statusCountMap, OrderStatus.EXCHANGED_GROUP);

        CompletedOrderResponse.CompletedOrderStatusCounts counts = CompletedOrderResponse.CompletedOrderStatusCounts.of(
            purchasedCount, canceledCount, returnedCount, exchangedCount
        );

        return new CompletedOrderResponse.CompletedOrderPageResponse(pageResponse, counts);
    }

    // ID 오름차순 기준으로 첫 번째 배송 정보를 선택하여 OrderSummary 조립
    private OrderSummary buildOrderSummary(Order order, Map<Long, OrderDelivery> deliveryMap) {
        OrderDelivery firstDelivery = order.getOrderItems().stream()
            .sorted(Comparator.comparingLong(OrderItem::getId))
            .map(item -> deliveryMap.get(item.getId()))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);

        // 배송 수취인이 있으면 우선 사용, 없으면 주문자 이름으로 fallback
        String recipient = order.getBuyerName();
        if (firstDelivery != null && firstDelivery.getReceiver() != null) {
            recipient = firstDelivery.getReceiver().getRecipientName();
        }

        // 결제일 기준 (결제 정보 없으면 주문일로 대체)
        LocalDateTime paidAt = order.getPayment() != null
            ? order.getPayment().getPaidAt()
            : order.getOrderDate();

        List<CompletedOrderResponse.OrderSummary.OrderItem> summaryItems = order.getOrderItems().stream()
            .map(item -> buildOrderItemSummary(item, deliveryMap))
            .filter(item -> item.status() != null)
            .toList();

        return OrderSummary.of(
            order.getId(),
            order.getOrderNumber(),
            paidAt,
            resolvePaidDayOfWeek(paidAt),
            recipient,
            summaryItems
        );
    }

    // 주문 항목 하나에 대한 배송사/운송장 정보를 포함한 요약 DTO 생성
    private CompletedOrderResponse.OrderSummary.OrderItem buildOrderItemSummary(
        OrderItem item, Map<Long, OrderDelivery> deliveryMap) {
        OrderDelivery itemDelivery = deliveryMap.get(item.getId());
        String deliveryCompany = null;
        String trackingNumber = null;
        if (itemDelivery != null && itemDelivery.getShipping() != null) {
            deliveryCompany = itemDelivery.getShipping().getCourierName();
            trackingNumber = itemDelivery.getShipping().getTrackingNumber();
        }
        return CompletedOrderResponse.OrderSummary.OrderItem.of(
            item.getId(),
            mapToCompletedOrderStatus(item.getOrderStatus()),
            deliveryCompany,
            trackingNumber,
            item.getProduct() != null ? item.getProduct().getTitle() : null,
            item.getQuantity()
        );
    }

    // LocalDateTime에서 커스텀 DayOfWeek enum으로 변환 (null-safe)
    private DayOfWeek resolvePaidDayOfWeek(LocalDateTime paidAt) {
        if (paidAt == null) {
            return null;
        }
        return DayOfWeek.valueOf(paidAt.getDayOfWeek().name());
    }

    // OrderStatus → CompletedOrderStatus 변환
    // 쿼리 레이어에서 완료 상태만 보장하나, 예상치 못한 상태에는 방어적으로 null 반환 후 상위에서 필터링
    private CompletedOrderStatus mapToCompletedOrderStatus(OrderStatus status) {
        if (OrderStatus.PURCHASE_CONFIRMED.equals(status)) {
            return CompletedOrderStatus.PURCHASED;
        } else if (OrderStatus.CANCELLED_GROUP.contains(status)) {
            return CompletedOrderStatus.CANCELED;
        } else if (OrderStatus.RETURNED_GROUP.contains(status)) {
            return CompletedOrderStatus.RETURNED;
        } else if (OrderStatus.EXCHANGED_GROUP.contains(status)) {
            return CompletedOrderStatus.EXCHANGED;
        }
        return null;
    }
}
