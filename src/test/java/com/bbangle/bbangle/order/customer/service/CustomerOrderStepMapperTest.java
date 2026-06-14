package com.bbangle.bbangle.order.customer.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderProgress;
import com.bbangle.bbangle.order.domain.model.CustomerOrderCategory;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위테스트] CustomerOrderStepMapper")
class CustomerOrderStepMapperTest {

    @Nested
    @DisplayName("일반(NORMAL) 플로우")
    class Normal {

        @Test
        @DisplayName("결제완료 → index 0")
        void paymentCompleted() {
            CustomerOrderProgress progress =
                CustomerOrderStepMapper.resolve(OrderStatus.PAYMENT_COMPLETED, null);

            assertThat(progress.category()).isEqualTo(CustomerOrderCategory.NORMAL);
            assertThat(progress.currentStep()).isEqualTo("결제완료");
            assertThat(progress.currentStepIndex()).isEqualTo(0);
            assertThat(progress.steps())
                .containsExactly("결제완료", "상품제작중", "상품발송", "배송완료", "구매확정");
        }

        @Test
        @DisplayName("발주확인(ORDER_CONFIRMED)은 소비자 화면에서 '상품제작중'으로 노출 → index 1")
        void orderConfirmedShownAsInProduction() {
            CustomerOrderProgress progress =
                CustomerOrderStepMapper.resolve(OrderStatus.ORDER_CONFIRMED, null);

            assertThat(progress.currentStep()).isEqualTo("상품제작중");
            assertThat(progress.currentStepIndex()).isEqualTo(1);
        }

        @Test
        @DisplayName("상품발송 + 배송 미완료 → 상품발송 index 2")
        void shippedNotDelivered() {
            CustomerOrderProgress progress =
                CustomerOrderStepMapper.resolve(OrderStatus.SHIPPED, OrderDeliveryStatus.DELIVERING);

            assertThat(progress.currentStep()).isEqualTo("상품발송");
            assertThat(progress.currentStepIndex()).isEqualTo(2);
        }

        @Test
        @DisplayName("상품발송 + 배송완료 → 배송완료 index 3")
        void shippedAndDelivered() {
            CustomerOrderProgress progress =
                CustomerOrderStepMapper.resolve(OrderStatus.SHIPPED, OrderDeliveryStatus.DELIVERED);

            assertThat(progress.currentStep()).isEqualTo("배송완료");
            assertThat(progress.currentStepIndex()).isEqualTo(3);
        }

        @Test
        @DisplayName("구매확정 → index 4")
        void purchaseConfirmed() {
            CustomerOrderProgress progress =
                CustomerOrderStepMapper.resolve(OrderStatus.PURCHASE_CONFIRMED, OrderDeliveryStatus.DELIVERED);

            assertThat(progress.currentStep()).isEqualTo("구매확정");
            assertThat(progress.currentStepIndex()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("반품(RETURN) 플로우")
    class Return {

        @Test
        @DisplayName("반품신청 → index 0")
        void requested() {
            CustomerOrderProgress progress =
                CustomerOrderStepMapper.resolve(OrderStatus.RETURN_REQUESTED, null);

            assertThat(progress.category()).isEqualTo(CustomerOrderCategory.RETURN);
            assertThat(progress.currentStepIndex()).isEqualTo(0);
            assertThat(progress.steps())
                .containsExactly("반품신청", "반품승인", "상품회수중", "상품확인중", "반품완료");
        }

        @Test
        @DisplayName("반품완료 → index 4")
        void completed() {
            CustomerOrderProgress progress =
                CustomerOrderStepMapper.resolve(OrderStatus.RETURN_COMPLETED, null);

            assertThat(progress.currentStepIndex()).isEqualTo(4);
        }

        @Test
        @DisplayName("반품거절/반품반려는 분기(종료) → index -1")
        void rejectedIsBranch() {
            assertThat(CustomerOrderStepMapper.resolve(OrderStatus.RETURN_REJECTED, null).currentStepIndex())
                .isEqualTo(-1);
            assertThat(CustomerOrderStepMapper.resolve(OrderStatus.RETURN_RETURNED, null).currentStepIndex())
                .isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("교환(EXCHANGE) 플로우")
    class Exchange {

        @Test
        @DisplayName("교환신청 → index 0, 단계 8개")
        void requested() {
            CustomerOrderProgress progress =
                CustomerOrderStepMapper.resolve(OrderStatus.EXCHANGE_REQUEST, null);

            assertThat(progress.category()).isEqualTo(CustomerOrderCategory.EXCHANGE);
            assertThat(progress.currentStepIndex()).isEqualTo(0);
            assertThat(progress.steps()).hasSize(8)
                .containsExactly("교환신청", "교환승인", "상품회수중", "상품확인중", "교환진행", "상품발송", "배송완료", "교환완료");
        }

        @Test
        @DisplayName("재배송 상품발송 + 배송완료 → 배송완료 index 6")
        void reshipDelivered() {
            CustomerOrderProgress progress =
                CustomerOrderStepMapper.resolve(OrderStatus.EXCHANGE_ITEM_SHIPPED, OrderDeliveryStatus.DELIVERED);

            assertThat(progress.currentStep()).isEqualTo("배송완료");
            assertThat(progress.currentStepIndex()).isEqualTo(6);
        }

        @Test
        @DisplayName("교환완료 → index 7")
        void completed() {
            CustomerOrderProgress progress =
                CustomerOrderStepMapper.resolve(OrderStatus.EXCHANGE_COMPLETED, null);

            assertThat(progress.currentStepIndex()).isEqualTo(7);
        }

        @Test
        @DisplayName("교환반려는 분기 → index -1")
        void returnedIsBranch() {
            assertThat(CustomerOrderStepMapper.resolve(OrderStatus.EXCHANGE_RETURNED, null).currentStepIndex())
                .isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("취소(CANCEL) 플로우")
    class Cancel {

        @Test
        @DisplayName("취소요청 → CANCEL 카테고리 index 0")
        void requested() {
            CustomerOrderProgress progress =
                CustomerOrderStepMapper.resolve(OrderStatus.CANCEL_REQUESTED, null);

            assertThat(progress.category()).isEqualTo(CustomerOrderCategory.CANCEL);
            assertThat(progress.currentStepIndex()).isEqualTo(0);
        }
    }
}
