package com.bbangle.bbangle.claim.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.claim.ReturnRequestFixture;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

@DisplayName("[단위 테스트] ReturnRequest")
class ReturnRequestTest {

    @Nested
    @DisplayName("approve()")
    class Approve {

        @Test
        @DisplayName("REQUESTED 상태에서 approve()를 호출하면 APPROVED로 변경되고 OrderItem도 RETURN_APPROVED가 된다")
        void approve_success_when_requested() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.requested(orderItem);

            // when
            returnRequest.approve("승인 사유");

            // then
            assertThat(returnRequest.getStatus()).isEqualTo(ReturnRequestRequestStatus.APPROVED);
        }

        @Test
        @DisplayName("APPROVED 상태에서 approve()를 호출하면 예외가 발생한다")
        void approve_fail_when_already_approved() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.approved(orderItem);

            // when & then
            assertThatThrownBy(() -> returnRequest.approve("승인 사유"))
                .isInstanceOf(BbangleException.class);
        }

        @Test
        @DisplayName("REJECTED 상태에서 approve()를 호출하면 예외가 발생한다")
        void approve_fail_when_already_rejected() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.rejected(orderItem);

            // when & then
            assertThatThrownBy(() -> returnRequest.approve("승인 사유"))
                .isInstanceOf(BbangleException.class);
        }
    }

    @Nested
    @DisplayName("startReturnPickup()")
    class StartReturnPickup {

        @Test
        @DisplayName("APPROVED 상태에서 호출하면 PICKUP_SCHEDULED로 변경된다")
        void startReturnPickup_success_when_approved() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_APPROVED);
            ReturnRequest returnRequest = ReturnRequestFixture.approved(orderItem);

            // when
            returnRequest.startReturnPickup();

            // then
            assertThat(returnRequest.getStatus())
                .isEqualTo(ReturnRequestRequestStatus.PICKUP_SCHEDULED);
        }

        @ParameterizedTest(name = "{0} 상태에서 호출하면 CLAIM_INVALID_STATUS 예외가 발생한다")
        @EnumSource(
            value = ReturnRequestRequestStatus.class,
            mode = Mode.EXCLUDE,
            names = "APPROVED"
        )
        @DisplayName("APPROVED 외 상태에서 호출하면 예외가 발생한다 (경계값 테스트)")
        void startReturnPickup_fails_on_invalid_status(ReturnRequestRequestStatus invalidStatus) {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.withStatus(invalidStatus, orderItem);

            // when & then
            assertThatThrownBy(returnRequest::startReturnPickup)
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);
        }

        @Test
        @DisplayName("PICKUP_SCHEDULED 상태에서 재호출하면 중복 수거 등록이 차단된다")
        void startReturnPickup_fails_when_already_pickup_scheduled() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.pickupScheduled(orderItem);

            // when & then
            assertThatThrownBy(returnRequest::startReturnPickup)
                .isInstanceOf(BbangleException.class);
        }
    }

    @Nested
    @DisplayName("reject()")
    class Reject {

        @Test
        @DisplayName("REQUESTED 상태에서 reject()를 호출하면 REJECTED로 변경되고 OrderItem도 RETURN_REJECTED가 된다")
        void reject_success_when_requested() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.requested(orderItem);

            // when
            returnRequest.reject("거절 사유");

            // then
            assertThat(returnRequest.getStatus()).isEqualTo(ReturnRequestRequestStatus.REJECTED);
        }

        @Test
        @DisplayName("APPROVED 상태에서 reject()를 호출하면 예외가 발생한다")
        void reject_fail_when_already_approved() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.approved(orderItem);

            // when & then
            assertThatThrownBy(() -> returnRequest.reject("거절 사유"))
                .isInstanceOf(BbangleException.class);
        }

        @Test
        @DisplayName("REJECTED 상태에서 reject()를 호출하면 예외가 발생한다")
        void reject_fail_when_already_rejected() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.rejected(orderItem);

            // when & then
            assertThatThrownBy(() -> returnRequest.reject("거절 사유"))
                .isInstanceOf(BbangleException.class);
        }
    }
}
