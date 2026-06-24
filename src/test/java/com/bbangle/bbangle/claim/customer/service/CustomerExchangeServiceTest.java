package com.bbangle.bbangle.claim.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.claim.customer.controller.dto.CustomerExchangeRequest;
import com.bbangle.bbangle.claim.repository.ExchangeRequestRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
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

@DisplayName("[단위 테스트] CustomerExchangeService - requestExchange")
@ExtendWith(MockitoExtension.class)
class CustomerExchangeServiceTest {

    @InjectMocks
    private CustomerExchangeService sut;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private OrderItemHistoryRepository orderItemHistoryRepository;

    @Nested
    @DisplayName("성공 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("SHIPPED·PURCHASE_CONFIRMED 상태의 주문 상품으로 교환 요청 시 EXCHANGE_REQUEST 로 전이되고 ExchangeRequest·OrderItemHistory가 누락 없이 저장된다")
        void givenShippedOrderItems_whenRequestExchange_thenSavedSuccessfully() {
            // given
            Long orderId = 1L;
            Long customerId = 100L;

            Order order = orderWithMember(orderId, customerId);

            OrderItem orderItem1 = OrderItemFixture.withId(
                OrderItemFixture.orderItemWithStatus(OrderStatus.SHIPPED), 301L);
            OrderItem orderItem2 = OrderItemFixture.withId(
                OrderItemFixture.orderItemWithStatus(OrderStatus.PURCHASE_CONFIRMED), 302L);

            CustomerExchangeRequest request = new CustomerExchangeRequest(
                List.of(301L, 302L),
                "사이즈 불일치 / 오배송"
            );

            given(orderRepository.findByIdWithMember(orderId)).willReturn(Optional.of(order));
            given(orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, List.of(301L, 302L)))
                .willReturn(List.of(orderItem1, orderItem2));

            // when
            sut.requestExchange(orderId, customerId, request);

            // then
            assertThat(orderItem1.getOrderStatus()).isEqualTo(OrderStatus.EXCHANGE_REQUEST);
            assertThat(orderItem2.getOrderStatus()).isEqualTo(OrderStatus.EXCHANGE_REQUEST);
            then(exchangeRequestRepository).should(times(1)).saveAll(anyList());
            then(orderItemHistoryRepository).should(times(1)).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("실패 시나리오")
    class FailureScenarios {

        @Test
        @DisplayName("타인의 주문 ID로 교환을 시도하면 ORDER_ACCESS_DENIED 예외가 발생한다")
        void givenOtherCustomersOrder_whenRequestExchange_thenThrowsOrderAccessDenied() {
            // given
            Long orderId = 1L;
            Long actualOwnerId = 100L;
            Long otherCustomerId = 999L;

            Order order = orderWithMember(orderId, actualOwnerId);

            CustomerExchangeRequest request = new CustomerExchangeRequest(
                List.of(301L),
                "교환 사유"
            );

            given(orderRepository.findByIdWithMember(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> sut.requestExchange(orderId, otherCustomerId, request))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.ORDER_ACCESS_DENIED);

            then(orderItemRepository).should(never()).findByOrderIdAndIdInWithOrder(orderId, List.of(301L));
            then(exchangeRequestRepository).should(never()).saveAll(anyList());
            then(orderItemHistoryRepository).should(never()).saveAll(anyList());
        }

        @Test
        @DisplayName("교환 불가 상태(PAYMENT_COMPLETED)의 주문 상품으로 교환 시도 시 CLAIM_INVALID_STATUS 예외가 선검증 단계에서 발생한다")
        void givenPaymentCompletedOrderItem_whenRequestExchange_thenThrowsClaimInvalidStatus() {
            // given
            Long orderId = 1L;
            Long customerId = 100L;

            Order order = orderWithMember(orderId, customerId);

            OrderItem invalidItem = OrderItemFixture.withId(
                OrderItemFixture.orderItemWithStatus(OrderStatus.PAYMENT_COMPLETED), 301L);

            CustomerExchangeRequest request = new CustomerExchangeRequest(
                List.of(301L),
                "교환 사유"
            );

            given(orderRepository.findByIdWithMember(orderId)).willReturn(Optional.of(order));
            given(orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, List.of(301L)))
                .willReturn(List.of(invalidItem));

            // when & then
            assertThatThrownBy(() -> sut.requestExchange(orderId, customerId, request))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            assertThat(invalidItem.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
            then(exchangeRequestRepository).should(never()).saveAll(anyList());
            then(orderItemHistoryRepository).should(never()).saveAll(anyList());
        }

        @Test
        @DisplayName("이미 취소된(CANCEL_APPROVED) 주문 상품으로 교환 시도 시 CLAIM_INVALID_STATUS 예외가 선검증 단계에서 발생한다")
        void givenCancelledOrderItem_whenRequestExchange_thenThrowsClaimInvalidStatus() {
            // given
            Long orderId = 1L;
            Long customerId = 100L;

            Order order = orderWithMember(orderId, customerId);

            OrderItem cancelledItem = OrderItemFixture.withId(
                OrderItemFixture.orderItemWithStatus(OrderStatus.CANCEL_APPROVED), 301L);

            CustomerExchangeRequest request = new CustomerExchangeRequest(
                List.of(301L),
                "교환 사유"
            );

            given(orderRepository.findByIdWithMember(orderId)).willReturn(Optional.of(order));
            given(orderItemRepository.findByOrderIdAndIdInWithOrder(orderId, List.of(301L)))
                .willReturn(List.of(cancelledItem));

            // when & then
            assertThatThrownBy(() -> sut.requestExchange(orderId, customerId, request))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            then(exchangeRequestRepository).should(never()).saveAll(anyList());
            then(orderItemHistoryRepository).should(never()).saveAll(anyList());
        }
    }

    private Order orderWithMember(Long orderId, Long memberId) {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(memberId);

        Order order = OrderFixture.defaultOrder()
            .member(member)
            .build();
        ReflectionTestUtils.setField(order, "id", orderId);

        return order;
    }
}
