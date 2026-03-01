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
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentModifyResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentRegisterResponse;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderConfirmCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.ShipmentModifyCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.ShipmentRegisterCommand;
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

    private Seller sellerWithStore(Long storeId) {
        Store store = StoreFixture.defaultStore();
        ReflectionTestUtils.setField(store, "id", storeId);
        return SellerFixture.createDefaultSeller(store);
    }

    @Nested
    @DisplayName("발주 확인 (confirmOrder)")
    class ConfirmOrderTest {

        @DisplayName("주문이 존재하지 않으면 ORDER_NOT_FOUND 예외가 발생한다.")
        @Test
        void givenNonExistingOrderId_whenConfirmOrder_thenThrowsException() {
            // given
            Long orderId = 999L;
            OrderConfirmCommand command = OrderConfirmCommand.builder()
                .sellerId(1L)
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

        @DisplayName("결제 완료(PAYMENT_COMPLETED)인 주문상품만 발주확인(ORDER_CONFIRMED)되고, 나머지는 failedIds로 반환된다.")
        @Test
        void givenMixedOrderItems_whenConfirmOrder_thenConfirmOnlyPaymentCompleted() {
            // given
            Long orderId = 1L;

            Order order = OrderFixture.createDefaultOrder();
            ReflectionTestUtils.setField(order, "id", orderId);

            OrderItem okItem = OrderItemFixture.createOrderItemWithStatus(OrderStatus.PAYMENT_COMPLETED);
            ReflectionTestUtils.setField(okItem, "id", 10L);

            OrderItem skipItem = OrderItemFixture.createOrderItemWithStatus(OrderStatus.ORDER_CONFIRMED);
            ReflectionTestUtils.setField(skipItem, "id", 11L);

            Seller seller = sellerWithStore(1L);

            OrderConfirmCommand command = OrderConfirmCommand.builder()
                .sellerId(1L)
                .orderId(orderId)
                .orderItemIds(List.of(10L, 11L))
                .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(sellerRepository.findByIdWithStore(1L)).willReturn(Optional.of(seller));
            given(orderItemRepository.findByOrderIdAndIdIn(orderId, List.of(10L, 11L)))
                .willReturn(List.of(okItem, skipItem));
            given(orderItemRepository.countOwnedOrderItems(orderId, List.of(10L, 11L), 1L))
                .willReturn(2L);

            // when
            OrderConfirmResponse result = sut.confirmOrder(command);

            // then
            then(orderRepository).should(times(1)).findById(orderId);
            then(orderItemRepository).should(times(1)).findByOrderIdAndIdIn(orderId, List.of(10L, 11L));

            assertThat(result).isNotNull();
            assertThat(result.content().orderId()).isEqualTo(orderId);
            assertThat(result.content().confirmedOrderItemIds()).containsExactly(10L);
            assertThat(result.content().failedOrderItemIds()).containsExactly(11L);

            assertThat(okItem.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CONFIRMED);
            assertThat(skipItem.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CONFIRMED); // 변경 없음
        }
    }

    @Nested
    @DisplayName("운송장 입력 (registerShipment)")
    class RegisterShipmentTest {

        @DisplayName("ORDER_CONFIRMED 상태의 주문상품에 운송장을 등록하면 SHIPPED로 전환되고 성공 목록에 포함된다.")
        @Test
        void givenOrderConfirmedItem_whenRegisterShipment_thenItemBecomesShippedAndSuccess() {
            // given
            Long orderId = 1L;
            Long sellerId = 1L;
            Long orderItemId = 10L;

            Order order = OrderFixture.createDefaultOrder();
            ReflectionTestUtils.setField(order, "id", orderId);

            OrderItem orderItem = OrderItemFixture.createOrderItemWithStatus(OrderStatus.ORDER_CONFIRMED);
            ReflectionTestUtils.setField(orderItem, "id", orderItemId);
            ReflectionTestUtils.setField(orderItem, "order", order);

            Seller seller = sellerWithStore(1L);

            ShipmentRegisterCommand command = ShipmentRegisterCommand.builder()
                .sellerId(sellerId)
                .orderId(orderId)
                .orderItemIds(List.of(orderItemId))
                .courierName("CJ대한통운")
                .trackingNumber("1234567890")
                .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(sellerRepository.findByIdWithStore(sellerId)).willReturn(Optional.of(seller));
            given(orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, List.of(orderItemId)))
                .willReturn(List.of(orderItem));
            given(orderItemRepository.countOwnedOrderItems(orderId, List.of(orderItemId), 1L))
                .willReturn(1L);
            given(orderDeliveryRepository.findByOrderItemIdIn(List.of(orderItemId)))
                .willReturn(Collections.emptyList()); // 기존 배송 정보 없음 → 신규 생성

            // when
            ShipmentRegisterResponse result = sut.registerShipment(command);

            // then
            assertThat(result.content().orderId()).isEqualTo(orderId);
            assertThat(result.content().successOrderItemIds()).containsExactly(orderItemId);
            assertThat(result.content().failedOrderItemIds()).isEmpty();
            assertThat(result.content().summary().successCount()).isEqualTo(1);
            assertThat(result.content().summary().failCount()).isEqualTo(0);
            assertThat(result.content().courierName()).isEqualTo("CJ대한통운");
            assertThat(result.content().trackingNumber()).isEqualTo("1234567890");
            assertThat(result.content().shippedAt()).isNotNull();
            // 주문상품 상태 전환 검증
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
        }

        @DisplayName("운송장 등록 대상 주문이 존재하지 않으면 ORDER_NOT_FOUND 예외가 발생한다.")
        @Test
        void givenNonExistingOrder_whenRegisterShipment_thenThrowsOrderNotFoundException() {
            // given
            ShipmentRegisterCommand command = ShipmentRegisterCommand.builder()
                .sellerId(1L)
                .orderId(999L)
                .orderItemIds(List.of(1L))
                .courierName("CJ대한통운")
                .trackingNumber("1234567890")
                .build();

            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            // when
            BbangleException result = assertThrows(BbangleException.class,
                () -> sut.registerShipment(command));

            // then
            assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_NOT_FOUND);
        }

        @DisplayName("ORDER_CONFIRMED/IN_PRODUCTION 이 아닌 주문상품은 운송장 등록이 실패하여 failedOrderItemIds에 포함된다.")
        @Test
        void givenInvalidStatusItem_whenRegisterShipment_thenItemFailsWithOrderInvalidStatus() {
            // given
            Long orderId = 1L;
            Long sellerId = 1L;
            Long orderItemId = 10L;

            Order order = OrderFixture.createDefaultOrder();
            ReflectionTestUtils.setField(order, "id", orderId);

            // PAYMENT_COMPLETED 상태 → shipOrder() 호출 시 ORDER_INVALID_STATUS 예외 → failedIds
            OrderItem orderItem = OrderItemFixture.createOrderItemWithStatus(OrderStatus.PAYMENT_COMPLETED);
            ReflectionTestUtils.setField(orderItem, "id", orderItemId);
            ReflectionTestUtils.setField(orderItem, "order", order);

            Seller seller = sellerWithStore(1L);

            ShipmentRegisterCommand command = ShipmentRegisterCommand.builder()
                .sellerId(sellerId)
                .orderId(orderId)
                .orderItemIds(List.of(orderItemId))
                .courierName("CJ대한통운")
                .trackingNumber("1234567890")
                .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(sellerRepository.findByIdWithStore(sellerId)).willReturn(Optional.of(seller));
            given(orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, List.of(orderItemId)))
                .willReturn(List.of(orderItem));
            given(orderItemRepository.countOwnedOrderItems(orderId, List.of(orderItemId), 1L))
                .willReturn(1L);
            given(orderDeliveryRepository.findByOrderItemIdIn(List.of(orderItemId)))
                .willReturn(Collections.emptyList());

            // when
            ShipmentRegisterResponse result = sut.registerShipment(command);

            // then: ORDER_INVALID_STATUS로 내부 처리 → 서비스 예외 없이 failedIds에 포함
            assertThat(result.content().successOrderItemIds()).isEmpty();
            assertThat(result.content().failedOrderItemIds()).containsExactly(orderItemId);
            assertThat(result.content().summary().failCount()).isEqualTo(1);
            // 상태 변경 없음
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
        }
    }

    @Nested
    @DisplayName("운송장 수정 (modifyShipment)")
    class ModifyShipmentTest {

        @DisplayName("PREPARING 상태의 배송 정보는 운송장 수정이 성공한다.")
        @Test
        void givenPreparingDelivery_whenModifyShipment_thenShipmentInfoUpdated() {
            // given
            Long orderId = 1L;
            Long sellerId = 1L;
            Long orderItemId = 10L;

            Order order = OrderFixture.createDefaultOrder();
            ReflectionTestUtils.setField(order, "id", orderId);

            OrderItem orderItem = OrderItemFixture.createOrderItemWithStatus(OrderStatus.SHIPPED);
            ReflectionTestUtils.setField(orderItem, "id", orderItemId);
            ReflectionTestUtils.setField(orderItem, "order", order);

            Seller seller = sellerWithStore(1L);

            // PREPARING 상태 → 수정 허용 (MODIFIABLE_STATUSES: NONE, PREPARING, PICKING_UP)
            OrderDelivery delivery = OrderDelivery.create(null, null,
                Shipping.of("기존택배사", "0000000000"),
                OrderDeliveryStatus.PREPARING,
                orderItem);
            ReflectionTestUtils.setField(delivery, "id", 100L);

            ShipmentModifyCommand command = ShipmentModifyCommand.builder()
                .sellerId(sellerId)
                .orderId(orderId)
                .orderItemIds(List.of(orderItemId))
                .courierName("한진택배")
                .trackingNumber("9876543210")
                .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(sellerRepository.findByIdWithStore(sellerId)).willReturn(Optional.of(seller));
            given(orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, List.of(orderItemId)))
                .willReturn(List.of(orderItem));
            given(orderItemRepository.countOwnedOrderItems(orderId, List.of(orderItemId), 1L))
                .willReturn(1L);
            given(orderDeliveryRepository.findByOrderItemIdIn(List.of(orderItemId)))
                .willReturn(List.of(delivery));

            // when
            ShipmentModifyResponse result = sut.modifyShipment(command);

            // then
            assertThat(result.content().orderId()).isEqualTo(orderId);
            assertThat(result.content().successOrderItemIds()).containsExactly(orderItemId);
            assertThat(result.content().failedOrderItemIds()).isEmpty();
            assertThat(result.content().courierName()).isEqualTo("한진택배");
            assertThat(result.content().trackingNumber()).isEqualTo("9876543210");
            // 실제 Shipping 필드 변경 여부 검증
            assertThat(delivery.getShipping().getCourierName()).isEqualTo("한진택배");
            assertThat(delivery.getShipping().getTrackingNumber()).isEqualTo("9876543210");
        }

        @DisplayName("배송 정보(OrderDelivery)가 없는 주문상품은 운송장 수정이 실패하여 failedOrderItemIds에 포함된다.")
        @Test
        void givenNoExistingDelivery_whenModifyShipment_thenItemFailsWithDeliveryNotFound() {
            // given
            Long orderId = 1L;
            Long sellerId = 1L;
            Long orderItemId = 10L;

            Order order = OrderFixture.createDefaultOrder();
            ReflectionTestUtils.setField(order, "id", orderId);

            OrderItem orderItem = OrderItemFixture.createOrderItemWithStatus(OrderStatus.SHIPPED);
            ReflectionTestUtils.setField(orderItem, "id", orderItemId);
            ReflectionTestUtils.setField(orderItem, "order", order);

            Seller seller = sellerWithStore(1L);

            ShipmentModifyCommand command = ShipmentModifyCommand.builder()
                .sellerId(sellerId)
                .orderId(orderId)
                .orderItemIds(List.of(orderItemId))
                .courierName("한진택배")
                .trackingNumber("9876543210")
                .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(sellerRepository.findByIdWithStore(sellerId)).willReturn(Optional.of(seller));
            given(orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, List.of(orderItemId)))
                .willReturn(List.of(orderItem));
            given(orderItemRepository.countOwnedOrderItems(orderId, List.of(orderItemId), 1L))
                .willReturn(1L);
            // 배송 정보 없음 → deliveryMap.get(orderItemId) == null → DELIVERY_NOT_FOUND
            given(orderDeliveryRepository.findByOrderItemIdIn(List.of(orderItemId)))
                .willReturn(Collections.emptyList());

            // when
            ShipmentModifyResponse result = sut.modifyShipment(command);

            // then: DELIVERY_NOT_FOUND(-784) 예외를 내부에서 catch → failedIds에 포함
            assertThat(result.content().successOrderItemIds()).isEmpty();
            assertThat(result.content().failedOrderItemIds()).containsExactly(orderItemId);
            assertThat(result.content().summary().failCount()).isEqualTo(1);
        }

        @DisplayName("DELIVERING 이후 상태의 배송 정보는 운송장 수정 시 DELIVERY_MODIFY_NOT_ALLOWED(-785)로 인해 failedOrderItemIds에 포함된다.")
        @Test
        void givenDeliveringDelivery_whenModifyShipment_thenItemFailsWithModifyNotAllowed() {
            // given
            Long orderId = 1L;
            Long sellerId = 1L;
            Long orderItemId = 10L;

            Order order = OrderFixture.createDefaultOrder();
            ReflectionTestUtils.setField(order, "id", orderId);

            OrderItem orderItem = OrderItemFixture.createOrderItemWithStatus(OrderStatus.SHIPPED);
            ReflectionTestUtils.setField(orderItem, "id", orderItemId);
            ReflectionTestUtils.setField(orderItem, "order", order);

            Seller seller = sellerWithStore(1L);

            // DELIVERING 상태 → MODIFIABLE_STATUSES(NONE, PREPARING, PICKING_UP) 외 → 수정 불가
            OrderDelivery delivery = OrderDelivery.create(null, null,
                Shipping.of("CJ대한통운", "1234567890"),
                OrderDeliveryStatus.DELIVERING,
                orderItem);
            ReflectionTestUtils.setField(delivery, "id", 100L);

            ShipmentModifyCommand command = ShipmentModifyCommand.builder()
                .sellerId(sellerId)
                .orderId(orderId)
                .orderItemIds(List.of(orderItemId))
                .courierName("한진택배")
                .trackingNumber("9999999999")
                .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(sellerRepository.findByIdWithStore(sellerId)).willReturn(Optional.of(seller));
            given(orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, List.of(orderItemId)))
                .willReturn(List.of(orderItem));
            given(orderItemRepository.countOwnedOrderItems(orderId, List.of(orderItemId), 1L))
                .willReturn(1L);
            given(orderDeliveryRepository.findByOrderItemIdIn(List.of(orderItemId)))
                .willReturn(List.of(delivery));

            // when
            ShipmentModifyResponse result = sut.modifyShipment(command);

            // then: DELIVERY_MODIFY_NOT_ALLOWED(-785) → failedIds에 포함
            assertThat(result.content().successOrderItemIds()).isEmpty();
            assertThat(result.content().failedOrderItemIds()).containsExactly(orderItemId);
            assertThat(result.content().summary().failCount()).isEqualTo(1);
            // 운송장 정보 변경 없음
            assertThat(delivery.getShipping().getCourierName()).isEqualTo("CJ대한통운");
            assertThat(delivery.getShipping().getTrackingNumber()).isEqualTo("1234567890");
        }

        @DisplayName("운송장 수정 대상 주문이 존재하지 않으면 ORDER_NOT_FOUND 예외가 발생한다.")
        @Test
        void givenNonExistingOrder_whenModifyShipment_thenThrowsOrderNotFoundException() {
            // given
            ShipmentModifyCommand command = ShipmentModifyCommand.builder()
                .sellerId(1L)
                .orderId(999L)
                .orderItemIds(List.of(1L))
                .courierName("한진택배")
                .trackingNumber("9876543210")
                .build();

            given(orderRepository.findById(999L)).willReturn(Optional.empty());

            // when
            BbangleException result = assertThrows(BbangleException.class,
                () -> sut.modifyShipment(command));

            // then
            assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("주문 검색 (orderSearch)")
    class OrderSearchTest {

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
}