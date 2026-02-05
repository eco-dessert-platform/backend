package com.bbangle.bbangle.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] OrderItem")
class OrderItemTest {

    @Nested
    @DisplayName("confirmOrder()")
    class ConfirmOrder {

        @Test
        @DisplayName("orderStatus가 PAYMENT_COMPLETED면 confirmOrder()는 true를 반환하고 ORDER_CONFIRMED로 변경된다")
        void confirmOrder_success_when_payment_completed() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.PAYMENT_COMPLETED);

            // when
            boolean result = orderItem.confirmOrder();

            // then
            assertThat(result).isTrue();
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CONFIRMED);
        }

        @Test
        @DisplayName("orderStatus가 PAYMENT_COMPLETED가 아니면 confirmOrder()는 false를 반환하고 상태는 유지된다")
        void confirmOrder_fail_when_not_payment_completed() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.ORDER_CONFIRMED);

            // when
            boolean result = orderItem.confirmOrder();

            // then
            assertThat(result).isFalse();
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.ORDER_CONFIRMED);
        }

        @Test
        @DisplayName("orderStatus가 null이어도 confirmOrder()는 false를 반환하고 null 상태를 유지한다")
        void confirmOrder_fail_when_status_is_null() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(null);

            // when
            boolean result = orderItem.confirmOrder();

            // then
            assertThat(result).isFalse();
            assertThat(orderItem.getOrderStatus()).isNull();
        }
    }

    @Nested
    @DisplayName("returnApprove()")
    class ReturnApprove {

        @Test
        @DisplayName("RETURN_REQUESTED 상태에서 returnApprove()를 호출하면 RETURN_APPROVED로 변경된다")
        void returnApprove_success_when_return_requested() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);

            // when
            orderItem.returnApprove();

            // then
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_APPROVED);
        }

        @Test
        @DisplayName("RETURN_REQUESTED가 아닌 상태에서 returnApprove()를 호출하면 예외가 발생한다")
        void returnApprove_fail_when_not_return_requested() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.ORDER_CONFIRMED);

            // when & then
            assertThatThrownBy(() -> orderItem.returnApprove())
                .isInstanceOf(BbangleException.class);
        }
    }

    @Nested
    @DisplayName("returnReject()")
    class ReturnReject {

        @Test
        @DisplayName("RETURN_REQUESTED 상태에서 returnReject()를 호출하면 RETURN_REJECTED로 변경된다")
        void returnReject_success_when_return_requested() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);

            // when
            orderItem.returnReject();

            // then
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REJECTED);
        }

        @Test
        @DisplayName("RETURN_REQUESTED가 아닌 상태에서 returnReject()를 호출하면 예외가 발생한다")
        void returnReject_fail_when_not_return_requested() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.ORDER_CONFIRMED);

            // when & then
            assertThatThrownBy(() -> orderItem.returnReject())
                .isInstanceOf(BbangleException.class);
        }
    }
}
