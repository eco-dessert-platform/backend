package com.bbangle.bbangle.claim.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.repository.ReturnRequestRepository;
import com.bbangle.bbangle.claim.seller.service.model.ReturnCreateCommand;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.OrderItemHistory;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.ReturnCreateResponse;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[단위 테스트] SellerReturnService - createReturn")
@ExtendWith(MockitoExtension.class)
class SellerReturnServiceCreateTest {

    @InjectMocks
    private SellerReturnService sut;

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @Mock
    private OrderItemHistoryRepository orderItemHistoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private SellerRepository sellerRepository;

    @DisplayName("SHIPPED 상태의 주문상품에 대해 반품 요청이 성공한다.")
    @Test
    void givenShippedOrderItem_whenCreateReturn_thenSuccess() {
        // given
        Long orderId = 1L;
        Long sellerId = 1L;
        Long storeId = 100L;

        Order order = newEntity(Order.class);
        ReflectionTestUtils.setField(order, "id", orderId);

        OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.SHIPPED);
        ReflectionTestUtils.setField(orderItem, "id", 10L);

        ReturnCreateCommand command = ReturnCreateCommand.builder()
            .sellerId(sellerId)
            .orderId(orderId)
            .orderItemIds(List.of(10L))
            .reason("상품 불량")
            .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(sellerRepository.findStoreIdBySellerId(sellerId)).willReturn(storeId);
        given(orderItemRepository.findByOrderIdAndIdIn(orderId, List.of(10L)))
            .willReturn(List.of(orderItem));
        given(orderItemRepository.countOwnedOrderItems(orderId, List.of(10L), storeId))
            .willReturn(1L);

        // when
        ReturnCreateResponse result = sut.createReturn(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content().orderId()).isEqualTo(orderId);
        assertThat(result.content().successOrderItemIds()).containsExactly(10L);
        assertThat(result.content().failedOrderItemIds()).isEmpty();
        assertThat(result.content().summary().requestedCount()).isEqualTo(1);
        assertThat(result.content().summary().successCount()).isEqualTo(1);
        assertThat(result.content().summary().failCount()).isEqualTo(0);
        assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED);

        then(returnRequestRepository).should(times(1)).save(any(ReturnRequest.class));
        then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
    }

    @DisplayName("PURCHASE_CONFIRMED 상태의 주문상품에 대해 반품 요청이 성공한다.")
    @Test
    void givenPurchaseConfirmedOrderItem_whenCreateReturn_thenSuccess() {
        // given
        Long orderId = 1L;
        Long sellerId = 1L;
        Long storeId = 100L;

        Order order = newEntity(Order.class);
        ReflectionTestUtils.setField(order, "id", orderId);

        OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.PURCHASE_CONFIRMED);
        ReflectionTestUtils.setField(orderItem, "id", 10L);

        ReturnCreateCommand command = ReturnCreateCommand.builder()
            .sellerId(sellerId)
            .orderId(orderId)
            .orderItemIds(List.of(10L))
            .reason("단순 변심")
            .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(sellerRepository.findStoreIdBySellerId(sellerId)).willReturn(storeId);
        given(orderItemRepository.findByOrderIdAndIdIn(orderId, List.of(10L)))
            .willReturn(List.of(orderItem));
        given(orderItemRepository.countOwnedOrderItems(orderId, List.of(10L), storeId))
            .willReturn(1L);

        // when
        ReturnCreateResponse result = sut.createReturn(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content().successOrderItemIds()).containsExactly(10L);
        assertThat(result.content().failedOrderItemIds()).isEmpty();
        assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED);

        then(returnRequestRepository).should(times(1)).save(any(ReturnRequest.class));
        then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
    }

    @DisplayName("혼합 상태(SHIPPED + ORDER_CONFIRMED)에서 SHIPPED만 반품 성공하고, ORDER_CONFIRMED는 실패한다.")
    @Test
    void givenMixedStatusOrderItems_whenCreateReturn_thenPartialSuccess() {
        // given
        Long orderId = 1L;
        Long sellerId = 1L;
        Long storeId = 100L;

        Order order = newEntity(Order.class);
        ReflectionTestUtils.setField(order, "id", orderId);

        OrderItem shippedItem = OrderItemFixture.orderItemWithStatus(OrderStatus.SHIPPED);
        ReflectionTestUtils.setField(shippedItem, "id", 10L);

        OrderItem confirmedItem = OrderItemFixture.orderItemWithStatus(OrderStatus.ORDER_CONFIRMED);
        ReflectionTestUtils.setField(confirmedItem, "id", 11L);

        ReturnCreateCommand command = ReturnCreateCommand.builder()
            .sellerId(sellerId)
            .orderId(orderId)
            .orderItemIds(List.of(10L, 11L))
            .reason("혼합 테스트")
            .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(sellerRepository.findStoreIdBySellerId(sellerId)).willReturn(storeId);
        given(orderItemRepository.findByOrderIdAndIdIn(orderId, List.of(10L, 11L)))
            .willReturn(List.of(shippedItem, confirmedItem));
        given(orderItemRepository.countOwnedOrderItems(orderId, List.of(10L, 11L), storeId))
            .willReturn(2L);

        // when
        ReturnCreateResponse result = sut.createReturn(command);

        // then
        assertThat(result.content().summary().requestedCount()).isEqualTo(2);
        assertThat(result.content().summary().successCount()).isEqualTo(1);
        assertThat(result.content().summary().failCount()).isEqualTo(1);
        assertThat(result.content().successOrderItemIds()).containsExactly(10L);
        assertThat(result.content().failedOrderItemIds()).containsExactly(11L);

        assertThat(shippedItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED);
        assertThat(confirmedItem.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CONFIRMED);

        then(returnRequestRepository).should(times(1)).save(any(ReturnRequest.class));
        then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
    }

    @DisplayName("허용되지 않는 상태(PAYMENT_COMPLETED)의 주문상품은 모두 실패한다.")
    @Test
    void givenInvalidStatusOrderItems_whenCreateReturn_thenAllFail() {
        // given
        Long orderId = 1L;
        Long sellerId = 1L;
        Long storeId = 100L;

        Order order = newEntity(Order.class);
        ReflectionTestUtils.setField(order, "id", orderId);

        OrderItem paymentCompletedItem = OrderItemFixture.orderItemWithStatus(OrderStatus.PAYMENT_COMPLETED);
        ReflectionTestUtils.setField(paymentCompletedItem, "id", 10L);

        ReturnCreateCommand command = ReturnCreateCommand.builder()
            .sellerId(sellerId)
            .orderId(orderId)
            .orderItemIds(List.of(10L))
            .reason("반품 테스트")
            .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(sellerRepository.findStoreIdBySellerId(sellerId)).willReturn(storeId);
        given(orderItemRepository.findByOrderIdAndIdIn(orderId, List.of(10L)))
            .willReturn(List.of(paymentCompletedItem));
        given(orderItemRepository.countOwnedOrderItems(orderId, List.of(10L), storeId))
            .willReturn(1L);

        // when
        ReturnCreateResponse result = sut.createReturn(command);

        // then
        assertThat(result.content().summary().successCount()).isEqualTo(0);
        assertThat(result.content().summary().failCount()).isEqualTo(1);
        assertThat(result.content().successOrderItemIds()).isEmpty();
        assertThat(result.content().failedOrderItemIds()).containsExactly(10L);

        assertThat(paymentCompletedItem.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);

        then(returnRequestRepository).should(never()).save(any(ReturnRequest.class));
        then(orderItemHistoryRepository).should(never()).save(any(OrderItemHistory.class));
    }

    @DisplayName("존재하지 않는 orderId이면 ORDER_NOT_FOUND 예외가 발생한다.")
    @Test
    void givenNonExistingOrderId_whenCreateReturn_thenThrowsException() {
        // given
        Long orderId = 999L;
        ReturnCreateCommand command = ReturnCreateCommand.builder()
            .sellerId(1L)
            .orderId(orderId)
            .orderItemIds(List.of(1L))
            .reason("반품 사유")
            .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.empty());

        // when
        BbangleException result = assertThrows(BbangleException.class,
            () -> sut.createReturn(command));

        // then
        assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_NOT_FOUND);
    }

    @DisplayName("소유권이 없는 주문상품이면 ORDER_ACCESS_DENIED 예외가 발생한다.")
    @Test
    void givenNonOwnerSeller_whenCreateReturn_thenThrowsException() {
        // given
        Long orderId = 1L;
        Long sellerId = 1L;
        Long storeId = 100L;

        Order order = newEntity(Order.class);
        ReflectionTestUtils.setField(order, "id", orderId);

        OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.SHIPPED);
        ReflectionTestUtils.setField(orderItem, "id", 10L);

        ReturnCreateCommand command = ReturnCreateCommand.builder()
            .sellerId(sellerId)
            .orderId(orderId)
            .orderItemIds(List.of(10L))
            .reason("반품 사유")
            .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(sellerRepository.findStoreIdBySellerId(sellerId)).willReturn(storeId);
        given(orderItemRepository.findByOrderIdAndIdIn(orderId, List.of(10L)))
            .willReturn(List.of(orderItem));
        given(orderItemRepository.countOwnedOrderItems(orderId, List.of(10L), storeId))
            .willReturn(0L);

        // when
        BbangleException result = assertThrows(BbangleException.class,
            () -> sut.createReturn(command));

        // then
        assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_ACCESS_DENIED);
    }

    @DisplayName("orderItemIds가 비어있으면 ORDER_ITEM_NOT_FOUND 예외가 발생한다.")
    @Test
    void givenEmptyOrderItemIds_whenCreateReturn_thenThrowsException() {
        // given
        ReturnCreateCommand command = ReturnCreateCommand.builder()
            .sellerId(1L)
            .orderId(1L)
            .orderItemIds(List.of())
            .reason("반품 사유")
            .build();

        // when
        BbangleException result = assertThrows(BbangleException.class,
            () -> sut.createReturn(command));

        // then
        assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_ITEM_NOT_FOUND);
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
}
