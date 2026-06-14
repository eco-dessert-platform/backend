package com.bbangle.bbangle.order.customer.service;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderInfo;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderItemInfo;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderPageResponse;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderProgress;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderStatusCounts;
import com.bbangle.bbangle.order.customer.repository.CustomerOrderRepository;
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
