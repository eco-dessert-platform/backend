package com.bbangle.bbangle.order.seller.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.seller.controller.dto.response.SellerOrderResponse.OrderConfirmResponse;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderConfirmCommand;
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
        then(orderItemRepository).should(times(1)).countOwnedOrderItems(orderId, List.of(10L, 11L), storeId);
        then(orderItemRepository).should(times(1)).findByOrderIdAndIdIn(orderId, List.of(10L, 11L));

        assertThat(result).isNotNull();
        assertThat(result.content()).isNotNull();
        assertThat(result.content().orderId()).isEqualTo(orderId);
        assertThat(result.content().summary()).isNotNull();
        assertThat(result.content().summary().requestedCount()).isEqualTo(2);
        assertThat(result.content().summary().successCount()).isEqualTo(1);
        assertThat(result.content().summary().failCount()).isEqualTo(1);
        assertThat(result.content().confirmedOrderItemIds())
            .asList()
            .containsExactly(10L);
        assertThat(result.content().failedOrderItemIds())
            .asList()
            .containsExactly(11L);

        // 상태 변경 검증
        assertThat(okItem.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CONFIRMED);
        assertThat(skipItem.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CONFIRMED);
    }

    @DisplayName("주문상품이 판매자 소유가 아니면 ORDER_ACCESS_DENIED 예외가 발생한다.")
    @Test
    void givenOrderItemNotOwnedBySeller_whenConfirmOrder_thenThrowsAccessDeniedException() {
        // given
        Long orderId = 1L;
        Long sellerId = 1L;
        Long storeId = 100L;

        Order order = newEntity(Order.class);
        ReflectionTestUtils.setField(order, "id", orderId);

        OrderConfirmCommand command = OrderConfirmCommand.builder()
            .sellerId(sellerId)
            .orderId(orderId)
            .orderItemIds(List.of(10L, 11L))
            .build();

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(sellerRepository.findStoreIdBySellerId(sellerId)).willReturn(storeId);
        given(orderItemRepository.countOwnedOrderItems(orderId, List.of(10L, 11L), storeId))
            .willReturn(1L); // 2개 요청했지만 1개만 소유 → 접근 거부

        // when
        BbangleException result = assertThrows(BbangleException.class,
            () -> sut.confirmOrder(command));

        // then
        then(orderRepository).should(times(1)).findById(orderId);
        then(sellerRepository).should(times(1)).findStoreIdBySellerId(sellerId);
        then(orderItemRepository).should(times(1)).countOwnedOrderItems(orderId, List.of(10L, 11L), storeId);
        assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_ACCESS_DENIED);
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
