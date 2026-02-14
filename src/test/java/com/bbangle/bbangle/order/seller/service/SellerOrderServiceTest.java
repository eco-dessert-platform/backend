package com.bbangle.bbangle.order.seller.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.delivery.domain.Receiver;
import com.bbangle.bbangle.delivery.domain.Sender;
import com.bbangle.bbangle.delivery.domain.Shipping;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderDeliveryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ShipmentRegisterResponse;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderConfirmCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.ShipmentRegisterCommand;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.model.PhoneNumberVO;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[비즈니스 로직] SellerOrderService")
@ExtendWith(MockitoExtension.class)
class SellerOrderServiceTest {

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

    @DisplayName("결제 완료(PAYMENT_COMPLETED)인 주문상품만 발주확인(ORDER_CONFIRMED)되며, 성공/실패 결과가 요약 정보로 반환된다.")
    @Test
    void givenMixedOrderItems_whenConfirmOrder_thenConfirmOnlyPaymentCompleted() {
        // given
        Long orderId = 1L;
        Long sellerId = 1L;
        Long storeId = 100L;

        Order order = newEntity(Order.class);
        ReflectionTestUtils.setField(order, "id", orderId);

        OrderItem okItem = newEntity(OrderItem.class);
        ReflectionTestUtils.setField(okItem, "id", 10L);
        ReflectionTestUtils.setField(okItem, "orderStatus", OrderStatus.PAYMENT_COMPLETED);
        ReflectionTestUtils.setField(okItem, "order", order);

        OrderItem skipItem = newEntity(OrderItem.class);
        ReflectionTestUtils.setField(skipItem, "id", 11L);
        ReflectionTestUtils.setField(skipItem, "orderStatus", OrderStatus.ORDER_CONFIRMED);
        ReflectionTestUtils.setField(skipItem, "order", order);

        OrderConfirmCommand command = OrderConfirmCommand.builder()
                .sellerId(sellerId)
                .orderId(orderId)
                .orderItemIds(List.of(10L, 11L))
                .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(sellerRepository.findStoreIdBySellerId(sellerId)).willReturn(storeId);
        given(orderItemRepository.countOwnedOrderItems(orderId, List.of(10L, 11L), storeId))
                .willReturn(2L);
        given(orderItemRepository.findByOrderIdAndIdIn(orderId, List.of(10L, 11L)))
                .willReturn(List.of(okItem, skipItem));

        // when
        OrderConfirmResponse result = sut.confirmOrder(command);

        // then
        then(orderRepository).should(times(1)).findById(orderId);
        then(sellerRepository).should(times(1)).findStoreIdBySellerId(sellerId);
        then(orderItemRepository).should(times(1)).findByOrderIdAndIdIn(orderId, List.of(10L, 11L));

        assertThat(result).isNotNull();
        assertThat(result.content().orderId()).isEqualTo(orderId);
        assertThat(result.content().confirmedOrderItemIds())
                .asList()
                .containsExactly(10L);

        assertThat(okItem.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CONFIRMED);
        assertThat(skipItem.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CONFIRMED);
    }

    private <T> T newEntity(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("운송장 입력 테스트")
    class RegisterShipmentTest {

        @DisplayName("orderItemIds가 null이면 ORDER_ITEM_NOT_FOUND 예외가 발생한다.")
        @Test
        void givenNullOrderItemIds_whenRegisterShipment_thenThrowsException() {
            // given
            ShipmentRegisterCommand command = ShipmentRegisterCommand.builder()
                    .sellerId(1L)
                    .orderId(1L)
                    .orderItemIds(null)
                    .courierName("CJ대한통운")
                    .trackingNumber("1234567890")
                    .build();

            // when
            BbangleException result = assertThrows(BbangleException.class,
                    () -> sut.registerShipment(command));

            // then
            assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        @DisplayName("orderItemIds가 비어있으면 ORDER_ITEM_NOT_FOUND 예외가 발생한다.")
        @Test
        void givenEmptyOrderItemIds_whenRegisterShipment_thenThrowsException() {
            // given
            ShipmentRegisterCommand command = ShipmentRegisterCommand.builder()
                    .sellerId(1L)
                    .orderId(1L)
                    .orderItemIds(List.of())
                    .courierName("CJ대한통운")
                    .trackingNumber("1234567890")
                    .build();

            // when
            BbangleException result = assertThrows(BbangleException.class,
                    () -> sut.registerShipment(command));

            // then
            assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        @DisplayName("주문이 존재하지 않으면 ORDER_NOT_FOUND 예외가 발생한다.")
        @Test
        void givenNonExistingOrderId_whenRegisterShipment_thenThrowsException() {
            // given
            Long orderId = 999L;
            ShipmentRegisterCommand command = ShipmentRegisterCommand.builder()
                    .sellerId(1L)
                    .orderId(orderId)
                    .orderItemIds(List.of(1L, 2L))
                    .courierName("CJ대한통운")
                    .trackingNumber("1234567890")
                    .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.empty());

            // when
            BbangleException result = assertThrows(BbangleException.class,
                    () -> sut.registerShipment(command));

            // then
            then(orderRepository).should(times(1)).findById(orderId);
            assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_NOT_FOUND);
        }

        @DisplayName("판매자가 존재하지 않으면 SELLER_NOT_FOUND 예외가 발생한다.")
        @Test
        void givenNonExistingSeller_whenRegisterShipment_thenThrowsException() {
            // given
            Long orderId = 1L;
            Long sellerId = 999L;

            Order order = newEntity(Order.class);
            ReflectionTestUtils.setField(order, "id", orderId);

            ShipmentRegisterCommand command = ShipmentRegisterCommand.builder()
                    .sellerId(sellerId)
                    .orderId(orderId)
                    .orderItemIds(List.of(1L, 2L))
                    .courierName("CJ대한통운")
                    .trackingNumber("1234567890")
                    .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(sellerRepository.findByIdWithStore(sellerId)).willReturn(Optional.empty());

            // when
            BbangleException result = assertThrows(BbangleException.class,
                    () -> sut.registerShipment(command));

            // then
            then(orderRepository).should(times(1)).findById(orderId);
            then(sellerRepository).should(times(1)).findByIdWithStore(sellerId);
            assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.SELLER_NOT_FOUND);
        }

        @DisplayName("판매자가 해당 주문상품의 소유자가 아니면 ORDER_ACCESS_DENIED 예외가 발생한다.")
        @Test
        void givenNonOwnerSeller_whenRegisterShipment_thenThrowsException() {
            // given
            Long orderId = 1L;
            Long sellerId = 1L;
            Long storeId = 100L;

            Order order = newEntity(Order.class);
            ReflectionTestUtils.setField(order, "id", orderId);

            Store store = newEntity(Store.class);
            ReflectionTestUtils.setField(store, "id", storeId);

            Seller seller = newEntity(Seller.class);
            ReflectionTestUtils.setField(seller, "id", sellerId);
            ReflectionTestUtils.setField(seller, "store", store);

            ShipmentRegisterCommand command = ShipmentRegisterCommand.builder()
                    .sellerId(sellerId)
                    .orderId(orderId)
                    .orderItemIds(List.of(1L, 2L))
                    .courierName("CJ대한통운")
                    .trackingNumber("1234567890")
                    .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(sellerRepository.findByIdWithStore(sellerId)).willReturn(Optional.of(seller));
            given(orderItemRepository.countOwnedOrderItems(orderId, List.of(1L, 2L), storeId))
                    .willReturn(0L);

            // when
            BbangleException result = assertThrows(BbangleException.class,
                    () -> sut.registerShipment(command));

            // then
            then(orderRepository).should(times(1)).findById(orderId);
            then(sellerRepository).should(times(1)).findByIdWithStore(sellerId);
            then(orderItemRepository).should(times(1))
                    .countOwnedOrderItems(orderId, List.of(1L, 2L), storeId);
            assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_ACCESS_DENIED);
        }

        @DisplayName("여러 주문상품에 대해 운송장 정보가 정상적으로 등록된다.")
        @Test
        void givenMultipleOrderItems_whenRegisterShipment_thenRegistersShipmentForAll() {
            // given
            Long orderId = 1L;
            Long sellerId = 1L;
            Long storeId = 100L;
            String courierName = "CJ대한통운";
            String trackingNumber = "1234567890";

            Order order = newEntity(Order.class);
            ReflectionTestUtils.setField(order, "id", orderId);
            ReflectionTestUtils.setField(order, "buyerName", "홍길동");
            ReflectionTestUtils.setField(order, "buyerPhone", "01098765432");

            OrderItem orderItem1 = newEntity(OrderItem.class);
            ReflectionTestUtils.setField(orderItem1, "id", 10L);
            ReflectionTestUtils.setField(orderItem1, "order", order);
            ReflectionTestUtils.setField(orderItem1, "orderStatus", OrderStatus.ORDER_CONFIRMED);

            OrderItem orderItem2 = newEntity(OrderItem.class);
            ReflectionTestUtils.setField(orderItem2, "id", 11L);
            ReflectionTestUtils.setField(orderItem2, "order", order);
            ReflectionTestUtils.setField(orderItem2, "orderStatus", OrderStatus.ORDER_CONFIRMED);

            Store store = newEntity(Store.class);
            ReflectionTestUtils.setField(store, "id", storeId);
            ReflectionTestUtils.setField(store, "name", "테스트 스토어");

            PhoneNumberVO phoneNumberVO = PhoneNumberVO.of("01012345678", null);

            Seller seller = newEntity(Seller.class);
            ReflectionTestUtils.setField(seller, "id", sellerId);
            ReflectionTestUtils.setField(seller, "store", store);
            ReflectionTestUtils.setField(seller, "phoneNumberVO", phoneNumberVO);
            ReflectionTestUtils.setField(seller, "originAddressLine", "서울시 강남구");
            ReflectionTestUtils.setField(seller, "originAddressDetail", "테헤란로 123");

            ShipmentRegisterCommand command = ShipmentRegisterCommand.builder()
                    .sellerId(sellerId)
                    .orderId(orderId)
                    .orderItemIds(List.of(10L, 11L))
                    .courierName(courierName)
                    .trackingNumber(trackingNumber)
                    .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(sellerRepository.findByIdWithStore(sellerId)).willReturn(Optional.of(seller));
            given(orderItemRepository.countOwnedOrderItems(orderId, List.of(10L, 11L), storeId))
                    .willReturn(2L);
            given(orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, List.of(10L, 11L)))
                    .willReturn(List.of(orderItem1, orderItem2));
            given(orderDeliveryRepository.findByOrderItemIdIn(List.of(10L, 11L)))
                    .willReturn(List.of());
            given(orderDeliveryRepository.save(any(OrderDelivery.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            ShipmentRegisterResponse result = sut.registerShipment(command);

            // then
            then(orderRepository).should(times(1)).findById(orderId);
            then(sellerRepository).should(times(1)).findByIdWithStore(sellerId);
            then(orderItemRepository).should(times(1))
                    .countOwnedOrderItems(orderId, List.of(10L, 11L), storeId);
            then(orderItemRepository).should(times(1))
                    .findByOrderIdAndIdInWithOrder(orderId, List.of(10L, 11L));
            then(orderDeliveryRepository).should(times(1)).findByOrderItemIdIn(List.of(10L, 11L));
            then(orderDeliveryRepository).should(times(2)).save(any(OrderDelivery.class));

            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.successOrderItemIds())
                    .asList()
                    .containsExactlyInAnyOrder(10L, 11L);
            assertThat(result.failedOrderItemIds())
                    .asList()
                    .isEmpty();
            assertThat(result.courierName()).isEqualTo(courierName);
            assertThat(result.trackingNumber()).isEqualTo(trackingNumber);
            assertThat(result.shippedAt()).isNotNull();

            assertThat(orderItem1.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(orderItem2.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
        }

        @DisplayName("기존 OrderDelivery가 있으면 해당 레코드를 업데이트한다.")
        @Test
        void givenExistingOrderDelivery_whenRegisterShipment_thenUpdatesExisting() {
            // given
            Long orderId = 1L;
            Long sellerId = 1L;
            Long storeId = 100L;
            String courierName = "CJ대한통운";
            String trackingNumber = "1234567890";

            Order order = newEntity(Order.class);
            ReflectionTestUtils.setField(order, "id", orderId);
            ReflectionTestUtils.setField(order, "buyerName", "홍길동");
            ReflectionTestUtils.setField(order, "buyerPhone", "01098765432");

            OrderItem orderItem1 = newEntity(OrderItem.class);
            ReflectionTestUtils.setField(orderItem1, "id", 10L);
            ReflectionTestUtils.setField(orderItem1, "order", order);
            ReflectionTestUtils.setField(orderItem1, "orderStatus", OrderStatus.ORDER_CONFIRMED);

            Store store = newEntity(Store.class);
            ReflectionTestUtils.setField(store, "id", storeId);
            ReflectionTestUtils.setField(store, "name", "테스트 스토어");

            Seller seller = newEntity(Seller.class);
            ReflectionTestUtils.setField(seller, "id", sellerId);
            ReflectionTestUtils.setField(seller, "store", store);

            Shipping shipping = Shipping.empty();
            OrderDelivery existingDelivery = OrderDelivery.create(
                    Sender.of("테스트 스토어", "01012345678", "서울시", "강남구", "12345"),
                    Receiver.of("홍길동", "01098765432", null, null, null, null),
                    shipping,
                    OrderDeliveryStatus.PREPARING,
                    orderItem1
            );
            ReflectionTestUtils.setField(existingDelivery, "id", 1L);

            ShipmentRegisterCommand command = ShipmentRegisterCommand.builder()
                    .sellerId(sellerId)
                    .orderId(orderId)
                    .orderItemIds(List.of(10L))
                    .courierName(courierName)
                    .trackingNumber(trackingNumber)
                    .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(sellerRepository.findByIdWithStore(sellerId)).willReturn(Optional.of(seller));
            given(orderItemRepository.countOwnedOrderItems(orderId, List.of(10L), storeId))
                    .willReturn(1L);
            given(orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, List.of(10L)))
                    .willReturn(List.of(orderItem1));
            given(orderDeliveryRepository.findByOrderItemIdIn(List.of(10L)))
                    .willReturn(List.of(existingDelivery));

            // when
            ShipmentRegisterResponse result = sut.registerShipment(command);

            // then
            then(orderDeliveryRepository).should(times(0)).save(any(OrderDelivery.class));

            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.successOrderItemIds())
                    .asList()
                    .containsExactly(10L);
            assertThat(result.failedOrderItemIds())
                    .asList()
                    .isEmpty();

            assertThat(existingDelivery.getStatus()).isEqualTo(OrderDeliveryStatus.DELIVERING);
            assertThat(existingDelivery.getShipping().getCourierName()).isEqualTo(courierName);
            assertThat(existingDelivery.getShipping().getTrackingNumber()).isEqualTo(trackingNumber);
        }

        @DisplayName("중복된 orderItemIds는 distinct 처리된다.")
        @Test
        void givenDuplicateOrderItemIds_whenRegisterShipment_thenDeduplicates() {
            // given
            Long orderId = 1L;
            Long sellerId = 1L;
            Long storeId = 100L;
            String courierName = "CJ대한통운";
            String trackingNumber = "1234567890";

            Order order = newEntity(Order.class);
            ReflectionTestUtils.setField(order, "id", orderId);
            ReflectionTestUtils.setField(order, "buyerName", "홍길동");
            ReflectionTestUtils.setField(order, "buyerPhone", "01098765432");

            OrderItem orderItem1 = newEntity(OrderItem.class);
            ReflectionTestUtils.setField(orderItem1, "id", 10L);
            ReflectionTestUtils.setField(orderItem1, "order", order);
            ReflectionTestUtils.setField(orderItem1, "orderStatus", OrderStatus.ORDER_CONFIRMED);

            Store store = newEntity(Store.class);
            ReflectionTestUtils.setField(store, "id", storeId);
            ReflectionTestUtils.setField(store, "name", "테스트 스토어");

            PhoneNumberVO phoneNumberVO = PhoneNumberVO.of("01012345678", null);

            Seller seller = newEntity(Seller.class);
            ReflectionTestUtils.setField(seller, "id", sellerId);
            ReflectionTestUtils.setField(seller, "store", store);
            ReflectionTestUtils.setField(seller, "phoneNumberVO", phoneNumberVO);
            ReflectionTestUtils.setField(seller, "originAddressLine", "서울시 강남구");
            ReflectionTestUtils.setField(seller, "originAddressDetail", "테헤란로 123");

            ShipmentRegisterCommand command = ShipmentRegisterCommand.builder()
                    .sellerId(sellerId)
                    .orderId(orderId)
                    .orderItemIds(List.of(10L, 10L, 10L))
                    .courierName(courierName)
                    .trackingNumber(trackingNumber)
                    .build();

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(sellerRepository.findByIdWithStore(sellerId)).willReturn(Optional.of(seller));
            given(orderItemRepository.countOwnedOrderItems(orderId, List.of(10L), storeId))
                    .willReturn(1L);
            given(orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, List.of(10L)))
                    .willReturn(List.of(orderItem1));
            given(orderDeliveryRepository.findByOrderItemIdIn(List.of(10L)))
                    .willReturn(List.of());
            given(orderDeliveryRepository.save(any(OrderDelivery.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            ShipmentRegisterResponse result = sut.registerShipment(command);

            // then
            then(orderItemRepository).should(times(1))
                    .countOwnedOrderItems(orderId, List.of(10L), storeId);
            then(orderItemRepository).should(times(1))
                    .findByOrderIdAndIdInWithOrder(orderId, List.of(10L));

            assertThat(result.successOrderItemIds())
                    .asList()
                    .containsExactly(10L);
        }
    }
}
