package com.bbangle.bbangle.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import java.time.LocalDateTime;
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
    @DisplayName("confirmPurchase()")
    class ConfirmPurchase {

        @Test
        @DisplayName("SHIPPED 상태에서 confirmPurchase()를 호출하면 PURCHASE_CONFIRMED로 전환되고 확정 시각이 기록된다")
        void confirmPurchase_success_when_shipped() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.SHIPPED);
            LocalDateTime confirmedAt = LocalDateTime.of(2025, 6, 29, 12, 0);

            // when
            orderItem.confirmPurchase(confirmedAt);

            // then
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_CONFIRMED);
            assertThat(orderItem.getPurchaseConfirmedAt()).isEqualTo(confirmedAt);
        }

        @Test
        @DisplayName("SHIPPED가 아닌 상태에서 confirmPurchase()를 호출하면 예외가 발생하고 상태/확정시각은 변경되지 않는다")
        void confirmPurchase_fail_when_not_shipped() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.PAYMENT_COMPLETED);

            // when & then
            assertThatThrownBy(() -> orderItem.confirmPurchase(LocalDateTime.now()))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.PURCHASE_CONFIRM_NOT_ALLOWED);
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
            assertThat(orderItem.getPurchaseConfirmedAt()).isNull();
        }

        @Test
        @DisplayName("canConfirmPurchase()는 SHIPPED 상태에서만 true를 반환한다")
        void canConfirmPurchase_true_only_when_shipped() {
            // given
            OrderItem shipped = OrderItemFixture.orderItemWithStatus(OrderStatus.SHIPPED);
            OrderItem confirmed = OrderItemFixture.orderItemWithStatus(OrderStatus.PURCHASE_CONFIRMED);

            // when & then
            assertThat(shipped.canConfirmPurchase()).isTrue();
            assertThat(confirmed.canConfirmPurchase()).isFalse();
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
