package com.bbangle.bbangle.order.customer.service;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.delivery.domain.Receiver;
import com.bbangle.bbangle.delivery.domain.Shipping;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerDeliveryInfo;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerOrderDetail;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerOrderDetailItem;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerPaymentSummary;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderInfo;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderItemInfo;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderPageResponse;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderProgress;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderStatusCounts;
import com.bbangle.bbangle.order.customer.repository.CustomerOrderRepository;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderDetailCommand;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderSearchCommand;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderDeliveryRepository;
import com.bbangle.bbangle.order.seller.controller.model.PaymentInfo;
import com.bbangle.bbangle.payment.domain.Payment;
import java.util.Collections;
import java.util.EnumSet;
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
public class CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final OrderDeliveryRepository orderDeliveryRepository;
    private final MemberRepository memberRepository;

    // 진행중 탭: 결제완료~상품발송 (구매확정 이전 일반 상태)
    private static final Set<OrderStatus> IN_PROGRESS_GROUP = Set.of(
        OrderStatus.PAYMENT_COMPLETED,
        OrderStatus.ORDER_CONFIRMED,
        OrderStatus.IN_PRODUCTION,
        OrderStatus.SHIPPED
    );

    // 결제금액/영수증 집계에서 제외할 상태 (정책: 반품·취소 금액 제외)
    private static final Set<OrderStatus> EXCLUDED_FROM_PAYMENT;

    static {
        EnumSet<OrderStatus> excluded = EnumSet.noneOf(OrderStatus.class);
        excluded.addAll(OrderStatus.CANCELLED_GROUP);
        excluded.addAll(OrderStatus.RETURNED_GROUP);
        EXCLUDED_FROM_PAYMENT = Collections.unmodifiableSet(excluded);
    }

    /**
     * 소비자 주문목록 조회.
     * 인증된 회원의 주문을 주문일 기준 최신순으로 페이징 조회하고,
     * 각 주문상품의 (주문상태 + 배송상태) 조합을 소비자 화면 진행단계로 변환하여 반환합니다.
     */
    @Transactional(readOnly = true)
    public CustomerOrderPageResponse getOrders(CustomerOrderSearchCommand command) {
        validateMember(command.memberId());

        BbanglePageResponse<Order> orderPage = customerOrderRepository.searchCustomerOrderList(command);
        Map<OrderStatus, Long> statusCountMap = customerOrderRepository.countByCustomerOrderStatus(command);

        // orderItems 는 fetchJoin 으로 초기화되어 있어야 합니다.
        Map<Long, OrderDelivery> latestDeliveryMap = fetchLatestDeliveries(orderPage.content());

        List<CustomerOrderInfo> orderInfos = orderPage.content().stream()
            .map(order -> buildOrderInfo(order, latestDeliveryMap))
            .toList();

        BbanglePageResponse<CustomerOrderInfo> ordersPage = new BbanglePageResponse<>(
            orderInfos,
            orderPage.page(),
            orderPage.size(),
            orderPage.totalPages(),
            orderPage.totalElements());

        CustomerOrderStatusCounts statusCounts = buildStatusCounts(statusCountMap);

        return new CustomerOrderPageResponse(ordersPage, statusCounts);
    }

    /**
     * 소비자 주문 상세 조회.
     * 본인 소유의 단일 주문을 조회하여 결제금액(반품·취소 제외)/배송지/상품 진행단계를 조립합니다.
     */
    @Transactional(readOnly = true)
    public CustomerOrderDetail getOrderDetail(CustomerOrderDetailCommand command) {
        validateMember(command.memberId());

        Order order = customerOrderRepository.findCustomerOrderDetail(command)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.CUSTOMER_ORDER_NOT_FOUND));

        Map<Long, OrderDelivery> latestDeliveryMap = fetchLatestDeliveries(List.of(order));

        List<CustomerOrderDetailItem> items = order.getOrderItems().stream()
            .map(item -> buildDetailItem(item, latestDeliveryMap.get(item.getId())))
            .toList();

        CustomerPaymentSummary payment = buildPaymentSummary(order);
        CustomerDeliveryInfo delivery = buildDeliveryInfo(order, latestDeliveryMap);

        return new CustomerOrderDetail(
            order.getId(),
            order.getOrderNumber(),
            order.getOrderDate() != null ? order.getOrderDate().toLocalDate() : null,
            payment,
            delivery,
            items
        );
    }

    /**
     * ① 결제 금액 블록 조립.
     * 정책에 따라 반품·취소 상태의 주문상품 금액은 집계에서 제외합니다.
     */
    private CustomerPaymentSummary buildPaymentSummary(Order order) {
        List<OrderItem> payableItems = order.getOrderItems().stream()
            .filter(item -> !EXCLUDED_FROM_PAYMENT.contains(item.getOrderStatus()))
            .toList();

        long productAmount = payableItems.stream()
            .mapToLong(item -> (long) safeInt(item.getProductPrice()) * safeInt(item.getQuantity()))
            .sum();
        long discountAmount = payableItems.stream()
            .mapToLong(item ->
                (long) (safeInt(item.getProductPrice()) - safeInt(item.getUnitPrice())) * safeInt(item.getQuantity()))
            .sum();
        // 결제 가능한 상품이 하나도 없으면(전체 반품·취소) 배송비도 청구하지 않습니다.
        long deliveryFee = payableItems.isEmpty() ? 0L : safeInt(order.getDeliveryFee());
        long finalAmount = productAmount - discountAmount + deliveryFee;

        Payment payment = order.getPayment();
        PaymentInfo paymentInfo = null;
        if (payment != null) {
            paymentInfo = PaymentInfo.of(
                payment.getPaymentStatus() != null ? payment.getPaymentStatus().getDescription() : null,
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().getDescription() : null,
                payment.getPaidAt());
        }

        boolean receiptViewable = payment != null && !payableItems.isEmpty();
        String discountMessage = discountAmount > 0
            ? String.format("총 %,d원 할인 받았어요", discountAmount)
            : null;

        return new CustomerPaymentSummary(
            productAmount,
            discountAmount,
            deliveryFee,
            finalAmount,
            discountAmount,
            discountMessage,
            receiptViewable,
            paymentInfo
        );
    }

    /**
     * ② 배송지 블록 조립.
     * 주문 내 배송은 일괄 처리되므로 대표 배송정보 1건을 사용합니다.
     * 배송지 변경 버튼은 결제완료 상태의 상품이 하나라도 있을 때만 활성화합니다.
     */
    private CustomerDeliveryInfo buildDeliveryInfo(Order order, Map<Long, OrderDelivery> deliveryMap) {
        boolean addressChangeable = order.getOrderItems().stream()
            .anyMatch(item -> item.getOrderStatus() == OrderStatus.PAYMENT_COMPLETED);

        OrderDelivery representative = order.getOrderItems().stream()
            .map(item -> deliveryMap.get(item.getId()))
            .filter(delivery -> delivery != null && delivery.getReceiver() != null)
            .findFirst()
            .orElse(null);

        if (representative == null) {
            return new CustomerDeliveryInfo(null, null, null, null, null, addressChangeable);
        }

        Receiver receiver = representative.getReceiver();
        Shipping shipping = representative.getShipping();

        return new CustomerDeliveryInfo(
            receiver.getRecipientName(),
            joinAddress(receiver.getRecipientAddress(), receiver.getRecipientAddressDetail()),
            receiver.getRecipientZipCode(),
            receiver.getRecipientPhone1(),
            shipping != null ? shipping.getDeliveryMemo() : null,
            addressChangeable
        );
    }

    private CustomerOrderDetailItem buildDetailItem(OrderItem item, OrderDelivery delivery) {
        OrderDeliveryStatus deliveryStatus = delivery != null ? delivery.getStatus() : null;

        String courierCompany = null;
        String trackingNumber = null;
        if (delivery != null && delivery.getShipping() != null) {
            courierCompany = delivery.getShipping().getCourierName();
            trackingNumber = delivery.getShipping().getTrackingNumber();
        }
        boolean deliveryTrackable = courierCompany != null && trackingNumber != null;

        Product product = item.getProduct();
        String storeName = product != null && product.getStore() != null ? product.getStore().getName() : null;
        String boardTitle = product != null && product.getBoard() != null ? product.getBoard().getTitle() : null;
        String productName = product != null ? product.getTitle() : null;
        List<String> tags = product != null ? product.getTags() : Collections.emptyList();

        int productPrice = safeInt(item.getProductPrice());
        int unitPrice = safeInt(item.getUnitPrice());

        CustomerOrderProgress progress = CustomerOrderStepMapper.resolve(item.getOrderStatus(), deliveryStatus);

        return new CustomerOrderDetailItem(
            item.getId(),
            storeName,
            boardTitle,
            productName,
            progress.currentStep(),
            item.getOrderStatus(),
            productPrice,
            unitPrice,
            calculateDiscountRate(productPrice, unitPrice),
            item.getQuantity(),
            safeInt(item.getTotalPrice()),
            tags,
            deliveryTrackable,
            courierCompany,
            trackingNumber,
            progress
        );
    }

    private int calculateDiscountRate(int productPrice, int unitPrice) {
        if (productPrice <= 0 || unitPrice >= productPrice) {
            return 0;
        }
        return (int) Math.round((productPrice - unitPrice) * 100.0 / productPrice);
    }

    private String joinAddress(String address, String addressDetail) {
        if (address == null) {
            return addressDetail;
        }
        if (addressDetail == null || addressDetail.isBlank()) {
            return address;
        }
        return address + " " + addressDetail;
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    /**
     * 주문 조회 요청자(회원) 검증.
     * - memberId 가 없으면(미인증) 401, 존재하지 않는 회원이면 404 로 응답합니다.
     */
    private void validateMember(Long memberId) {
        if (memberId == null) {
            throw new BbangleException(BbangleErrorCode.CUSTOMER_ORDER_UNAUTHORIZED);
        }
        if (!memberRepository.existsById(memberId)) {
            throw new BbangleException(BbangleErrorCode.CUSTOMER_ORDER_MEMBER_NOT_FOUND);
        }
    }

    private CustomerOrderInfo buildOrderInfo(Order order, Map<Long, OrderDelivery> deliveryMap) {
        List<CustomerOrderItemInfo> items = order.getOrderItems().stream()
            .map(item -> buildOrderItemInfo(item, deliveryMap.get(item.getId())))
            .toList();

        Payment payment = order.getPayment();
        PaymentInfo paymentInfo = null;
        if (payment != null) {
            paymentInfo = PaymentInfo.of(
                payment.getPaymentStatus() != null ? payment.getPaymentStatus().getDescription() : null,
                payment.getPaymentMethod() != null ? payment.getPaymentMethod().getDescription() : null,
                payment.getPaidAt());
        }

        return new CustomerOrderInfo(
            order.getId(),
            order.getOrderNumber(),
            order.getOrderDate() != null ? order.getOrderDate().toLocalDate() : null,
            order.getTotalAmount() != null ? order.getTotalAmount().longValue() : null,
            paymentInfo,
            items
        );
    }

    private CustomerOrderItemInfo buildOrderItemInfo(OrderItem item, OrderDelivery delivery) {
        OrderDeliveryStatus deliveryStatus = delivery != null ? delivery.getStatus() : null;

        String deliveryStatusLabel = deliveryStatus != null ? deliveryStatus.getDescription() : null;
        String courierCompany = null;
        String trackingNumber = null;
        if (delivery != null && delivery.getShipping() != null) {
            courierCompany = delivery.getShipping().getCourierName();
            trackingNumber = delivery.getShipping().getTrackingNumber();
        }

        CustomerOrderProgress progress = CustomerOrderStepMapper.resolve(item.getOrderStatus(), deliveryStatus);

        return new CustomerOrderItemInfo(
            item.getId(),
            item.getProduct() != null && item.getProduct().getBoard() != null
                ? item.getProduct().getBoard().getTitle() : null,
            item.getProduct() != null ? item.getProduct().getTitle() : null,
            item.getQuantity(),
            item.getUnitPrice() != null ? item.getUnitPrice().longValue() : null,
            item.getTotalPrice() != null ? item.getTotalPrice().longValue() : null,
            item.getOrderStatus(),
            deliveryStatusLabel,
            courierCompany,
            trackingNumber,
            progress
        );
    }

    private CustomerOrderStatusCounts buildStatusCounts(Map<OrderStatus, Long> countMap) {
        return CustomerOrderStatusCounts.of(
            sumCounts(countMap, IN_PROGRESS_GROUP),
            sumCounts(countMap, Set.of(OrderStatus.PURCHASE_CONFIRMED)),
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
}
