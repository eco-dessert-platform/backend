package com.bbangle.bbangle.claim.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.claim.ReturnRequestFixture;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
