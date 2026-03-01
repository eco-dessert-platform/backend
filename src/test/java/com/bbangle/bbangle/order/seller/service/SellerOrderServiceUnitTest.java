package com.bbangle.bbangle.order.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.delivery.domain.Shipping;
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
import com.bbangle.bbangle.order.domain.OrderDelivery;
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

            assertThat(response.orderNumber()).isEqualTo(orderNumber);
            assertThat(response.totalOrderPrice()).isEqualTo(totalAmount.toString());
            assertThat(response.recipientName()).isEqualTo(buyerName);
            assertThat(response.orderItems()).isNotNull().asList().hasSize(1);

            var orderItemList = response.orderItems().get(0);
            assertThat(orderItemList.orderNumber()).isEqualTo(orderNumber);
            assertThat(orderItemList.orderStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
            assertThat(orderItemList.orderItemInfo().itemName()).isEqualTo("카카오커피");
            assertThat(orderItemList.orderItemInfo().quantity()).isEqualTo(2);
            assertThat(orderItemList.orderItemInfo().unitPrice()).isEqualTo(25000L);
            assertThat(orderItemList.orderItemInfo().totalPrice()).isEqualTo(50000L);

            assertThat(orderItemList.orderDeliveryStatus()).isEqualTo(OrderDeliveryStatus.PREPARING);
            assertThat(orderItemList.courierCompany()).isEqualTo(CourierCompany.NONE);
            assertThat(orderItemList.trackingNumber()).isEqualTo("-");

            assertThat(response.sellerId()).isEqualTo(1L);
            assertThat(response.paymentInfo()).isNotNull();
        }

        @DisplayName("OrderItem이 없는 주문은 빈 orderItems 목록으로 응답에 포함된다.")
        @Test
        void givenOrdersWithMissingOrderItem_whenOrderSearch_thenIncludesOrderWithEmptyItemsList() {
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

            // invalidOrder에 OrderItem이 없으므로 수집되는 ID는 validOrder의 10L만
            given(orderRepository.searchOrderList(command)).willReturn(orderPage);
            given(orderDeliveryRepository.findLatestByOrderItemIds(List.of(10L)))
                .willReturn(Collections.emptyList());

            // when
            BbanglePageResponse<OrderResponse.OrderSearchResponse> result = sut.orderSearch(command);

            // then: 두 주문 모두 응답에 포함됨 (skip 없음)
            assertThat(result.content()).asList().hasSize(2);
            assertThat(result.content().get(0).orderNumber()).isEqualTo(validOrder.getOrderNumber());
            // invalidOrder는 orderItems가 빈 목록
            assertThat(result.content().get(1).orderItems()).isEmpty();
        }

        @DisplayName("Payment가 없는 주문은 paymentInfo가 null인 상태로 응답에 포함된다.")
        @Test
        void givenOrdersWithMissingPayment_whenOrderSearch_thenIncludesOrderWithNullPaymentInfo() {
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

            // 비정상 주문 (Payment 없음 - null 상태)
            Order invalidOrder = OrderFixture.createDefaultOrder();
            ReflectionTestUtils.setField(invalidOrder, "id", 2L);
            OrderItem invalidOrderItem = OrderItemFixture.createOrderItemWithProduct(product);
            ReflectionTestUtils.setField(invalidOrderItem, "id", 11L);
            invalidOrder.addOrderItem(invalidOrderItem);
            // payment 설정하지 않음 (null)

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

            // then: 두 주문 모두 응답에 포함됨 (skip 없음)
            assertThat(result.content()).asList().hasSize(2);
            // validOrder: paymentInfo 있음
            assertThat(result.content().get(0).paymentInfo()).isNotNull();
            // invalidOrder: paymentInfo null (payment 없음)
            assertThat(result.content().get(1).paymentInfo()).isNull();
        }
    }
}
