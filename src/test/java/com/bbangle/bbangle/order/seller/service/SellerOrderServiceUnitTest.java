package com.bbangle.bbangle.order.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderItemFixture;
import com.bbangle.bbangle.fixture.payment.domain.PaymentFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.CompletedOrderSearchType;
import com.bbangle.bbangle.order.domain.model.CourierCompany;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderDeliveryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderConfirmCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[단위 테스트] SellerOrderService")
@ExtendWith(MockitoExtension.class)
@Slf4j
class SellerOrderServiceUnitTest {

    @InjectMocks
    private SellerOrderService sut;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderDeliveryRepository orderDeliveryRepository;

    @Mock
    private SellerRepository sellerRepository;

    @DisplayName("주문이 존재하지 않으면 ORDER_NOT_FOUND 예외가 발생한다.")
    @Test
    void givenNonExistingOrderId_whenConfirmOrder_thenThrowsException() {
        // given
        Long orderId = 999L;
        OrderConfirmCommand command = OrderConfirmCommand.builder()
            .sellerId(1L) // 지금은 검증 안 해도 command 형태 맞추려고 넣어둠
            .orderId(orderId)
            .orderItemIds(List.of(1L, 2L))
            .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.empty());

        // when
        BbangleException result = assertThrows(BbangleException.class,
            () -> sut.confirmOrder(command));

        // then
        then(orderRepository).should(times(1)).findById(orderId);
        assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_NOT_FOUND);
    }

    @DisplayName("결제 완료(PAYMENT_COMPLETED)인 주문상품만 발주확인(ORDER_CONFIRMED)되고, 나머지는 부분 성공으로 스킵된다.")
    @Test
    void givenMixedOrderItems_whenConfirmOrder_thenConfirmOnlyPaymentCompleted() {
        // given
        Long orderId = 1L;

        Order order = OrderFixture.createDefaultOrder();
        ReflectionTestUtils.setField(order, "id", orderId);

        OrderItem okItem = OrderItemFixture.createOrderItemWithStatus(OrderStatus.PAYMENT_COMPLETED);
        ReflectionTestUtils.setField(okItem, "id", 10L);
        ReflectionTestUtils.setField(okItem, "order", order);

        OrderItem skipItem = OrderItemFixture.createOrderItemWithStatus(OrderStatus.ORDER_CONFIRMED);
        ReflectionTestUtils.setField(skipItem, "id", 11L);
        ReflectionTestUtils.setField(skipItem, "order", order);

        OrderConfirmCommand command = OrderConfirmCommand.builder()
            .sellerId(1L)
            .orderId(orderId)
            .orderItemIds(List.of(10L, 11L))
            .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(sellerRepository.findStoreIdBySellerId(1L)).willReturn(1L);
        given(orderItemRepository.countOwnedOrderItems(orderId, List.of(10L, 11L), 1L))
            .willReturn(2L);
        given(orderItemRepository.findByOrderIdAndIdIn(orderId, List.of(10L, 11L)))
            .willReturn(List.of(okItem, skipItem));

        // when
        OrderConfirmResponse result = sut.confirmOrder(command);

        // then
        then(orderRepository).should(times(1)).findById(orderId);
        then(orderItemRepository).should(times(1)).findByOrderIdAndIdIn(orderId, List.of(10L, 11L));

        // 응답 검증: PAYMENT_COMPLETED였던 것만 포함
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.confirmedOrderItemIds())
            .asList()
            .containsExactly(10L);

        // 상태 변경 검증
        assertThat(okItem.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CONFIRMED);
        assertThat(skipItem.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CONFIRMED); // 원래부터 confirmed였으면 그대로
    }

    @DisplayName("주문 검색 시 Page 객체로 올바르게 반환되는지 확인")
    @Test
    void givenOrderSearchRequest_whenOrderSearch_thenReturnsPageWithCorrectData() {
        // given
        Long orderId = 1L;
        String orderNumber = "ORDER-2025-01-01-00001";
        Integer totalAmount = 50000;
        String buyerName = "홍길동";

        Store boardStore = StoreFixture.defaultStore();
        ReflectionTestUtils.setField(boardStore, "id", 2L);
        var board = BoardFixture.defaultBoardWithStore(boardStore, "카카오커피");
        var product = ProductFixture.create(board, "카카오커피");

        Order order = OrderFixture.createOrderWithNumber(orderNumber)
            .toBuilder()
            .totalAmount(totalAmount)
            .buyerName(buyerName)
            .build();
        ReflectionTestUtils.setField(order, "id", orderId);

        OrderItem orderItem = OrderItemFixture.createOrderItemWithProduct(product);
        ReflectionTestUtils.setField(orderItem, "id", 10L);
        order.addOrderItem(orderItem);

        Store store = StoreFixture.defaultStore();
        ReflectionTestUtils.setField(store, "id", 1L);

        Seller seller = SellerFixture.createDefaultSeller(store);
        ReflectionTestUtils.setField(seller, "id", 1L);
        ReflectionTestUtils.setField(order, "seller", seller);

        Payment payment = PaymentFixture.createDefaultPayment(order);
        ReflectionTestUtils.setField(order, "payment", payment);

        PageRequest pageable = PageRequest.of(0, 10);
        BbanglePageResponse<Order> orderPage = new BbanglePageResponse<>(
            List.of(order),
            0,
            10,
            1,
            1L);

        OrderSearchCommand command = OrderSearchCommand.builder()
            .sellerId(1L)
            .orderDeliveryStatus(null)
            .searchType(CompletedOrderSearchType.ORDER_NUMBER)
            .searchCondition(null)
            .page(pageable)
            .build();

        given(orderRepository.searchOrderList(command)).willReturn(orderPage);
        given(orderDeliveryRepository.findLatestByOrderItemIds(List.of(10L)))
            .willReturn(Collections.emptyList());

        // when
        BbanglePageResponse<OrderResponse.OrderSearchResponse> result = sut.orderSearch(command);

        log.info("=== OrderSearchResponse (JSON) ===");
        if (!result.content().isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result.content().get(0));
                log.info(json);
            } catch (Exception e) {
                log.error("Failed to serialize OrderSearchResponse to JSON", e);
            }
        }

        // then
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content()).asList().hasSize(1);

        OrderResponse.OrderSearchResponse response = result.content().get(0);

        // 기본 정보 검증
        assertThat(response.orderNumber()).isEqualTo(orderNumber);
        assertThat(response.totalOrderPrice()).isEqualTo(totalAmount.toString());
        assertThat(response.recipientName()).isEqualTo(buyerName);

        // 주문 상품 정보 검증
        assertThat(response.orderItems()).isNotNull().asList().hasSize(1);

        var orderItemList = response.orderItems().get(0);
        assertThat(orderItemList.orderNumber()).isEqualTo(orderNumber);
        assertThat(orderItemList.orderStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
        assertThat(orderItemList.orderItemInfo().itemName()).isEqualTo("카카오커피");
        assertThat(orderItemList.orderItemInfo().quantity()).isEqualTo(2);
        assertThat(orderItemList.orderItemInfo().unitPrice()).isEqualTo(25000L);
        assertThat(orderItemList.orderItemInfo().totalPrice()).isEqualTo(50000L);

        // 배송 정보 검증 (OrderItem에 OrderDelivery 없으므로 기본값 - 이제 품목 수준)
        assertThat(orderItemList.orderDeliveryStatus()).isEqualTo(OrderDeliveryStatus.PREPARING);
        assertThat(orderItemList.courierCompany()).isEqualTo(CourierCompany.NONE);
        assertThat(orderItemList.trackingNumber()).isEqualTo("-");

        // 판매자 정보 검증
        assertThat(response.sellerId()).isEqualTo(1L);

        // 결제 정보 검증
        assertThat(response.paymentInfo()).isNotNull();
    }

    @DisplayName("OrderItem이 없는 주문은 스킵되고 나머지는 정상 조회된다")
    @Test
    void givenOrdersWithMissingOrderItem_whenOrderSearch_thenSkipsInvalidOrders() {
        // given
        Store boardStore = StoreFixture.defaultStore();
        ReflectionTestUtils.setField(boardStore, "id", 2L);
        var board = BoardFixture.defaultBoardWithStore(boardStore, "카카오커피");
        var product = ProductFixture.create(board, "카카오커피");

        // 정상 주문 (OrderItem 있음)
        Order validOrder = OrderFixture.createDefaultOrder();
        ReflectionTestUtils.setField(validOrder, "id", 1L);
        OrderItem validOrderItem = OrderItemFixture.createOrderItemWithProduct(product);
        ReflectionTestUtils.setField(validOrderItem, "id", 10L);
        validOrder.addOrderItem(validOrderItem);
        Payment validPayment = PaymentFixture.createDefaultPayment(validOrder);
        ReflectionTestUtils.setField(validOrder, "payment", validPayment);

        // 비정상 주문 (OrderItem 없음)
        Order invalidOrder = OrderFixture.createDefaultOrder();
        ReflectionTestUtils.setField(invalidOrder, "id", 2L);
        // OrderItem을 추가하지 않음
        Payment invalidPayment = PaymentFixture.createDefaultPayment(invalidOrder);
        ReflectionTestUtils.setField(invalidOrder, "payment", invalidPayment);

        BbanglePageResponse<Order> orderPage = new BbanglePageResponse<>(
            List.of(validOrder, invalidOrder),
            0, 10, 1, 2L
        );

        OrderSearchCommand command = OrderSearchCommand.builder()
            .sellerId(1L)
            .orderDeliveryStatus(null)
            .searchType(CompletedOrderSearchType.ORDER_NUMBER)
            .page(PageRequest.of(0, 10))
            .build();

        given(orderRepository.searchOrderList(command)).willReturn(orderPage);
        given(orderDeliveryRepository.findLatestByOrderItemIds(List.of(10L)))
            .willReturn(Collections.emptyList());

        // when
        BbanglePageResponse<OrderResponse.OrderSearchResponse> result = sut.orderSearch(command);

        // then
        assertThat(result.content()).asList().hasSize(1);  // 정상 주문 1개만
        assertThat(result.content().get(0).orderNumber()).isEqualTo(validOrder.getOrderNumber());
    }

    @DisplayName("Payment가 없는 주문은 스킵되고 나머지는 정상 조회된다")
    @Test
    void givenOrdersWithMissingPayment_whenOrderSearch_thenSkipsInvalidOrders() {
        // given
        Store boardStore = StoreFixture.defaultStore();
        ReflectionTestUtils.setField(boardStore, "id", 2L);
        var board = BoardFixture.defaultBoardWithStore(boardStore, "카카오커피");
        var product = ProductFixture.create(board, "카카오커피");

        // 정상 주문 (Payment 있음)
        Order validOrder = OrderFixture.createDefaultOrder();
        ReflectionTestUtils.setField(validOrder, "id", 1L);
        OrderItem validOrderItem = OrderItemFixture.createOrderItemWithProduct(product);
        ReflectionTestUtils.setField(validOrderItem, "id", 10L);
        validOrder.addOrderItem(validOrderItem);
        Payment validPayment = PaymentFixture.createDefaultPayment(validOrder);
        ReflectionTestUtils.setField(validOrder, "payment", validPayment);

        // 비정상 주문 (Payment 없음)
        Order invalidOrder = OrderFixture.createDefaultOrder();
        ReflectionTestUtils.setField(invalidOrder, "id", 2L);
        OrderItem invalidOrderItem = OrderItemFixture.createOrderItemWithProduct(product);
        ReflectionTestUtils.setField(invalidOrderItem, "id", 11L);
        invalidOrder.addOrderItem(invalidOrderItem);
        // Payment를 설정하지 않음 (null)

        BbanglePageResponse<Order> orderPage = new BbanglePageResponse<>(
            List.of(validOrder, invalidOrder),
            0, 10, 1, 2L
        );

        OrderSearchCommand command = OrderSearchCommand.builder()
            .sellerId(1L)
            .page(PageRequest.of(0, 10))
            .build();

        given(orderRepository.searchOrderList(command)).willReturn(orderPage);
        given(orderDeliveryRepository.findLatestByOrderItemIds(List.of(10L, 11L)))
            .willReturn(Collections.emptyList());

        // when
        BbanglePageResponse<OrderResponse.OrderSearchResponse> result = sut.orderSearch(command);

        // then
        assertThat(result.content()).asList().hasSize(1);
        assertThat(result.content().get(0).orderNumber()).isEqualTo(validOrder.getOrderNumber());
    }
}