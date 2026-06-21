package com.bbangle.bbangle.claim.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.claim.customer.controller.dto.CustomerReturnRequest;
import com.bbangle.bbangle.claim.repository.ReturnRequestRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
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

@DisplayName("[단위 테스트] CustomerReturnService - requestReturn")
@ExtendWith(MockitoExtension.class)
class CustomerReturnServiceTest {

    @InjectMocks
    private CustomerReturnService sut;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @Mock
    private OrderItemHistoryRepository orderItemHistoryRepository;

    @Nested
    @DisplayName("성공 시나리오")
    class SuccessScenarios {

        @Test
        @DisplayName("SHIPPED 상태의 주문 상품으로 반품 요청 시 RETURN_REQUESTED로 전이되고 ReturnRequest·OrderItemHistory가 누락 없이 저장된다")
        void givenShippedOrderItems_whenRequestReturn_thenSavedSuccessfully() {
            // given
            Long orderId = 1L;
            Long customerId = 100L;

            Order order = orderWithMember(orderId, customerId);

            OrderItem orderItem1 = OrderItemFixture.orderItemWithStatus(OrderStatus.SHIPPED);
            ReflectionTestUtils.setField(orderItem1, "id", 201L);

            OrderItem orderItem2 = OrderItemFixture.orderItemWithStatus(OrderStatus.PURCHASE_CONFIRMED);
            ReflectionTestUtils.setField(orderItem2, "id", 202L);

            CustomerReturnRequest request = new CustomerReturnRequest(
                List.of(201L, 202L),
                "단순 변심 / 사이즈 불일치"
            );

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(orderItemRepository.findByOrderIdAndIdIn(orderId, List.of(201L, 202L)))
                .willReturn(List.of(orderItem1, orderItem2));

            // when
            sut.requestReturn(orderId, customerId, request);

            // then
            assertThat(orderItem1.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED);
            assertThat(orderItem2.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED);
            then(returnRequestRepository).should(times(1)).saveAll(anyList());
            then(orderItemHistoryRepository).should(times(1)).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("실패 시나리오")
    class FailureScenarios {

        @Test
        @DisplayName("타인의 주문 ID로 반품을 시도하면 ORDER_ACCESS_DENIED 예외가 발생한다")
        void givenOtherCustomersOrder_whenRequestReturn_thenThrowsOrderAccessDenied() {
            // given
            Long orderId = 1L;
            Long actualOwnerId = 100L;
            Long otherCustomerId = 999L;

            Order order = orderWithMember(orderId, actualOwnerId);

            CustomerReturnRequest request = new CustomerReturnRequest(
                List.of(201L),
                "반품 사유"
            );

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            // when & then
            assertThatThrownBy(() -> sut.requestReturn(orderId, otherCustomerId, request))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.ORDER_ACCESS_DENIED);

            then(orderItemRepository).should(never()).findByOrderIdAndIdIn(orderId, List.of(201L));
            then(returnRequestRepository).should(never()).saveAll(anyList());
            then(orderItemHistoryRepository).should(never()).saveAll(anyList());
        }

        @Test
        @DisplayName("반품 불가 상태(PAYMENT_COMPLETED)의 주문 상품으로 반품 시도 시 CLAIM_INVALID_STATUS 예외가 발생한다")
        void givenInvalidStatusOrderItem_whenRequestReturn_thenThrowsClaimInvalidStatus() {
            // given
            Long orderId = 1L;
            Long customerId = 100L;

            Order order = orderWithMember(orderId, customerId);

            OrderItem invalidItem = OrderItemFixture.orderItemWithStatus(OrderStatus.PAYMENT_COMPLETED);
            ReflectionTestUtils.setField(invalidItem, "id", 201L);

            CustomerReturnRequest request = new CustomerReturnRequest(
                List.of(201L),
                "반품 사유"
            );

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(orderItemRepository.findByOrderIdAndIdIn(orderId, List.of(201L)))
                .willReturn(List.of(invalidItem));

            // when & then
            assertThatThrownBy(() -> sut.requestReturn(orderId, customerId, request))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            assertThat(invalidItem.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
            then(returnRequestRepository).should(never()).saveAll(anyList());
            then(orderItemHistoryRepository).should(never()).saveAll(anyList());
        }

        @Test
        @DisplayName("이미 취소된(CANCEL_APPROVED) 주문 상품으로 반품 시도 시 CLAIM_INVALID_STATUS 예외가 발생한다")
        void givenCancelledOrderItem_whenRequestReturn_thenThrowsClaimInvalidStatus() {
            // given
            Long orderId = 1L;
            Long customerId = 100L;

            Order order = orderWithMember(orderId, customerId);

            OrderItem cancelledItem = OrderItemFixture.orderItemWithStatus(OrderStatus.CANCEL_APPROVED);
            ReflectionTestUtils.setField(cancelledItem, "id", 201L);

            CustomerReturnRequest request = new CustomerReturnRequest(
                List.of(201L),
                "반품 사유"
            );

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(orderItemRepository.findByOrderIdAndIdIn(orderId, List.of(201L)))
                .willReturn(List.of(cancelledItem));

            // when & then
            assertThatThrownBy(() -> sut.requestReturn(orderId, customerId, request))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            then(returnRequestRepository).should(never()).saveAll(anyList());
            then(orderItemHistoryRepository).should(never()).saveAll(anyList());
        }
    }

    private Order orderWithMember(Long orderId, Long memberId) {
        Member member = newEntity(Member.class);
        ReflectionTestUtils.setField(member, "id", memberId);

        Order order = newEntity(Order.class);
        ReflectionTestUtils.setField(order, "id", orderId);
        ReflectionTestUtils.setField(order, "member", member);

        return order;
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
