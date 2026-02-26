package com.bbangle.bbangle.order.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.delivery.domain.Receiver;
import com.bbangle.bbangle.delivery.domain.Shipping;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderItemFixture;
import com.bbangle.bbangle.order.domain.OrderDelivery;
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
import org.junit.jupiter.api.Nested;
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

        Store store = StoreFixture.defaultStore();
        ReflectionTestUtils.setField(store, "id", 1L);
        Seller seller = SellerFixture.createDefaultSeller(store);
        ReflectionTestUtils.setField(seller, "id", 1L);

        OrderConfirmCommand command = OrderConfirmCommand.builder()
            .sellerId(1L)
            .orderId(orderId)
            .orderItemIds(List.of(10L, 11L))
            .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(sellerRepository.findByIdWithStore(1L)).willReturn(Optional.of(seller));
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
        assertThat(result.content().orderId()).isEqualTo(orderId);
        assertThat(result.content().confirmedOrderItemIds())
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
        given(orderRepository.countByOrderStatus(command)).willReturn(Collections.emptyMap());
        given(orderDeliveryRepository.findLatestByOrderItemIds(List.of(10L)))
            .willReturn(Collections.emptyList());

        // when
        OrderResponse.OrderSearchPageResponse result = sut.orderSearch(command);

        log.info("=== OrderSearchResponse (JSON) ===");
        if (!result.orders().content().isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result.orders().content().get(0));
                log.info(json);
            } catch (Exception e) {
                log.error("Failed to serialize OrderSearchResponse to JSON", e);
            }
        }

        // then
        assertThat(result).isNotNull();
        assertThat(result.orders().totalElements()).isEqualTo(1L);
        assertThat(result.orders().content()).asList().hasSize(1);

        OrderResponse.OrderSearchResponse response = result.orders().content().get(0);

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

    @DisplayName("OrderItem이 없는 주문은 orderItems가 빈 목록으로 포함되어 조회된다")
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
        given(orderRepository.countByOrderStatus(command)).willReturn(Collections.emptyMap());
        given(orderDeliveryRepository.findLatestByOrderItemIds(List.of(10L)))
            .willReturn(Collections.emptyList());

        // when
        OrderResponse.OrderSearchPageResponse result = sut.orderSearch(command);

        // then
        // OrderItem이 없는 주문도 응답에 포함되지만 orderItems가 비어있음
        assertThat(result.orders().content()).asList().hasSize(2);
        assertThat(result.orders().content().get(0).orderNumber()).isEqualTo(validOrder.getOrderNumber());
        assertThat(result.orders().content().get(0).orderItems()).isNotEmpty();
        assertThat(result.orders().content().get(1).orderNumber()).isEqualTo(invalidOrder.getOrderNumber());
        assertThat(result.orders().content().get(1).orderItems()).isEmpty();
    }

    @DisplayName("Payment가 없는 주문은 paymentInfo가 null로 포함되어 조회된다")
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
        given(orderRepository.countByOrderStatus(command)).willReturn(Collections.emptyMap());
        given(orderDeliveryRepository.findLatestByOrderItemIds(List.of(10L, 11L)))
            .willReturn(Collections.emptyList());

        // when
        OrderResponse.OrderSearchPageResponse result = sut.orderSearch(command);

        // then
        // Payment가 없는 주문도 응답에 포함되지만 paymentInfo가 null
        assertThat(result.orders().content()).asList().hasSize(2);
        assertThat(result.orders().content().get(0).orderNumber()).isEqualTo(validOrder.getOrderNumber());
        assertThat(result.orders().content().get(0).paymentInfo()).isNotNull();
        assertThat(result.orders().content().get(1).orderNumber()).isEqualTo(invalidOrder.getOrderNumber());
        assertThat(result.orders().content().get(1).paymentInfo()).isNull();
    }

    @Nested
    @DisplayName("주문 품목 상세 조회 테스트")
    class SearchOrderItemDetailsTest {

        @DisplayName("orderItemIds가 null이면 ORDER_ITEM_NOT_FOUND 예외가 발생한다.")
        @Test
        void givenNullOrderItemIds_whenSearchOrderItemDetails_thenThrowsException() {
            // given
            List<Long> orderItemIds = null;
            Long sellerId = 1L;

            // when
            BbangleException result = assertThrows(BbangleException.class,
                () -> sut.searchOrderItemDetails(orderItemIds, sellerId));

            // then
            assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        @DisplayName("판매자 소유가 아닌 주문 품목 접근 시 ORDER_ACCESS_DENIED 예외가 발생한다.")
        @Test
        void givenNonOwnedOrderItems_whenSearchOrderItemDetails_thenThrowsAccessDeniedException() {
            // given
            Long sellerId = 1L;
            Long storeId = 100L;
            List<Long> orderItemIds = List.of(1L);

            given(sellerRepository.findStoreIdBySellerId(sellerId)).willReturn(storeId);
            given(orderItemRepository.countOwnedOrderItemsByStoreId(orderItemIds, storeId)).willReturn(0L);

            // when
            BbangleException result = assertThrows(BbangleException.class,
                () -> sut.searchOrderItemDetails(orderItemIds, sellerId));

            // then
            assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }

        @DisplayName("배송 정보가 없으면 Order의 기본값으로 BuyerInfo가 구성된다.")
        @Test
        void givenNoDelivery_whenSearchOrderItemDetails_thenBuyerInfoUsesOrderData() {
            // given
            Long sellerId = 1L;
            Long storeId = 100L;
            List<Long> orderItemIds = List.of(1L);

            Order order = OrderFixture.createDefaultOrder(); // buyerName="홍길동", buyerPhone="01012345678"
            Store boardStore = StoreFixture.defaultStore();
            var board = BoardFixture.defaultBoardWithStore(boardStore, "테스트 게시글");
            var product = ProductFixture.create(board, "테스트 상품");

            OrderItem orderItem = OrderItemFixture.createOrderItemWithStatus(OrderStatus.SHIPPED);
            ReflectionTestUtils.setField(orderItem, "id", 1L);
            ReflectionTestUtils.setField(orderItem, "order", order);
            ReflectionTestUtils.setField(orderItem, "product", product);

            given(sellerRepository.findStoreIdBySellerId(sellerId)).willReturn(storeId);
            given(orderItemRepository.countOwnedOrderItemsByStoreId(orderItemIds, storeId)).willReturn(1L);
            given(orderItemRepository.findWithOrderAndProductByIdIn(orderItemIds)).willReturn(List.of(orderItem));
            given(orderDeliveryRepository.findLatestByOrderItemIds(orderItemIds)).willReturn(Collections.emptyList());

            // when
            List<OrderResponse.OrderItemDetailResponse> result = sut.searchOrderItemDetails(orderItemIds, sellerId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).buyer().recipientName()).isEqualTo("홍길동");
            assertThat(result.get(0).buyer().buyerPhone1()).isEqualTo("01012345678");
            assertThat(result.get(0).orderInfo().orderStatusLabel()).isEqualTo(OrderStatus.SHIPPED.getDescription());
        }

        @DisplayName("배송 정보가 있으면 택배사 정보가 응답에 포함되고 JSON으로 출력된다.")
        @Test
        void givenDeliveryWithShipping_whenSearchOrderItemDetails_thenPrintsJsonAndReturnsCorrectData()
            throws Exception {
            // given
            Long sellerId = 1L;
            Long storeId = 100L;
            List<Long> orderItemIds = List.of(1L);

            Order order = OrderFixture.createDefaultOrder();
            Store boardStore = StoreFixture.defaultStore();
            var board = BoardFixture.defaultBoardWithStore(boardStore, "테스트 게시글");
            var product = ProductFixture.create(board, "테스트 상품");

            OrderItem orderItem = OrderItemFixture.createOrderItemWithStatus(OrderStatus.SHIPPED);
            ReflectionTestUtils.setField(orderItem, "id", 1L);
            ReflectionTestUtils.setField(orderItem, "order", order);
            ReflectionTestUtils.setField(orderItem, "product", product);

            Receiver receiver = Receiver.of("수취인", "010-9999-8888", null,
                "서울시 강남구 예제로 123", "101호", "12345");
            Shipping shipping = Shipping.of("CJ대한통운", "1234-5678-910");
            OrderDelivery delivery = OrderDelivery.create(
                null, receiver, shipping, OrderDeliveryStatus.DELIVERING, orderItem);

            given(sellerRepository.findStoreIdBySellerId(sellerId)).willReturn(storeId);
            given(orderItemRepository.countOwnedOrderItemsByStoreId(orderItemIds, storeId)).willReturn(1L);
            given(orderItemRepository.findWithOrderAndProductByIdIn(orderItemIds)).willReturn(List.of(orderItem));
            given(orderDeliveryRepository.findLatestByOrderItemIds(orderItemIds)).willReturn(List.of(delivery));

            // when
            List<OrderResponse.OrderItemDetailResponse> result = sut.searchOrderItemDetails(orderItemIds, sellerId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).shipping().courierCompany()).isEqualTo("CJ대한통운");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(result.get(0));
            log.info("=== OrderItemDetailResponse (JSON) ===");
            log.info(json);
        }
    }
}