package com.bbangle.bbangle.payment.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.order.domain.OrderFixture;
import com.bbangle.bbangle.fixture.payment.domain.PaymentFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.payment.client.PaymentClient;
import com.bbangle.bbangle.payment.client.dto.PaymentConfirmResult;
import com.bbangle.bbangle.payment.customer.dto.request.PaymentConfirmRequest;
import com.bbangle.bbangle.payment.customer.dto.response.PaymentConfirmResponse;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.payment.domain.PaymentStatus;
import com.bbangle.bbangle.payment.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[단위 테스트] CustomerPaymentService")
@ExtendWith(MockitoExtension.class)
class CustomerPaymentServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private CustomerPaymentService customerPaymentService;

    private static final Long MEMBER_ID = 1L;
    private static final Long OTHER_MEMBER_ID = 2L;
    private static final String ORDER_NUMBER = "ORD20250101120000ABCD1234";
    private static final String ORDER_GROUP_ID = "groupid123456789012345678901";
    private static final String PAYMENT_KEY = "toss_pay_key_12345";
    private static final Long CORRECT_AMOUNT = 50000L;

    private Order order;
    private Payment pendingPayment;

    @BeforeEach
    void setUp() {
        // totalAmount=50000, orderGroupId·orderNumber·member 세팅
        order = OrderFixture.createOrderWithAmount(CORRECT_AMOUNT.intValue());
        ReflectionTestUtils.setField(order, "orderNumber", ORDER_NUMBER);
        ReflectionTestUtils.setField(order, "orderGroupId", ORDER_GROUP_ID);

        Member member = Member.builder().id(MEMBER_ID).name("테스터").build();
        ReflectionTestUtils.setField(order, "member", member);

        // Payment는 order와 연결
        pendingPayment = PaymentFixture.createPaymentWithStatus(order, PaymentStatus.PENDING);
    }

    @Nested
    @DisplayName("결제 승인 — 성공")
    class ConfirmSuccess {

        @Test
        @DisplayName("올바른 요청이면 Payment 상태가 COMPLETED로 변경된다")
        void success() {
            // given
            given(orderRepository.findByOrderNumber(ORDER_NUMBER)).willReturn(Optional.of(order));
            given(paymentRepository.findByOrderGroupIdWithLock(ORDER_GROUP_ID))
                .willReturn(List.of(pendingPayment));
            given(paymentClient.confirm(PAYMENT_KEY, ORDER_NUMBER, CORRECT_AMOUNT))
                .willReturn(new PaymentConfirmResult(PAYMENT_KEY, LocalDateTime.now()));

            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, ORDER_NUMBER, CORRECT_AMOUNT
            );

            // when
            PaymentConfirmResponse response = customerPaymentService.confirm(MEMBER_ID, request);

            // then
            assertThat(response.orderNumber()).isEqualTo(ORDER_NUMBER);
            assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.COMPLETED.name());
            assertThat(pendingPayment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }
    }

    @Nested
    @DisplayName("결제 승인 — 실패: 주문 없음")
    class OrderNotFound {

        @Test
        @DisplayName("존재하지 않는 orderNumber이면 ORDER_NOT_FOUND 예외가 발생한다")
        void orderNotFound() {
            // given
            given(orderRepository.findByOrderNumber(ORDER_NUMBER)).willReturn(Optional.empty());

            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, ORDER_NUMBER, CORRECT_AMOUNT
            );

            // when & then
            assertThatThrownBy(() -> customerPaymentService.confirm(MEMBER_ID, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.ORDER_NOT_FOUND));

            verify(paymentRepository, never()).findByOrderGroupIdWithLock(anyString());
            verify(paymentClient, never()).confirm(anyString(), anyString(), anyLong());
        }
    }

    @Nested
    @DisplayName("결제 승인 — 실패: 접근 권한 없음")
    class OrderAccessDenied {

        @Test
        @DisplayName("다른 회원의 주문이면 ORDER_ACCESS_DENIED 예외가 발생한다")
        void accessDenied() {
            // given
            given(orderRepository.findByOrderNumber(ORDER_NUMBER)).willReturn(Optional.of(order));

            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, ORDER_NUMBER, CORRECT_AMOUNT
            );

            // when & then — OTHER_MEMBER_ID로 접근
            assertThatThrownBy(() -> customerPaymentService.confirm(OTHER_MEMBER_ID, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.ORDER_ACCESS_DENIED));

            verify(paymentRepository, never()).findByOrderGroupIdWithLock(anyString());
        }
    }

    @Nested
    @DisplayName("결제 승인 — 실패: 결제 정보 없음")
    class PaymentNotFound {

        @Test
        @DisplayName("그룹 내 결제 정보가 없으면 PAYMENT_NOT_FOUND 예외가 발생한다")
        void paymentNotFound() {
            // given
            given(orderRepository.findByOrderNumber(ORDER_NUMBER)).willReturn(Optional.of(order));
            given(paymentRepository.findByOrderGroupIdWithLock(ORDER_GROUP_ID)).willReturn(List.of());

            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, ORDER_NUMBER, CORRECT_AMOUNT
            );

            // when & then
            assertThatThrownBy(() -> customerPaymentService.confirm(MEMBER_ID, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.PAYMENT_NOT_FOUND));

            verify(paymentClient, never()).confirm(anyString(), anyString(), anyLong());
        }
    }

    @Nested
    @DisplayName("결제 승인 — 실패: 이미 완료된 결제")
    class PaymentAlreadyCompleted {

        @Test
        @DisplayName("COMPLETED 상태 결제에 재승인 요청하면 PAYMENT_ALREADY_COMPLETED 예외가 발생한다")
        void alreadyCompleted() {
            // given
            Payment completedPayment = PaymentFixture.createPaymentWithStatus(order, PaymentStatus.COMPLETED);

            given(orderRepository.findByOrderNumber(ORDER_NUMBER)).willReturn(Optional.of(order));
            given(paymentRepository.findByOrderGroupIdWithLock(ORDER_GROUP_ID))
                .willReturn(List.of(completedPayment));

            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, ORDER_NUMBER, CORRECT_AMOUNT
            );

            // when & then
            assertThatThrownBy(() -> customerPaymentService.confirm(MEMBER_ID, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.PAYMENT_ALREADY_COMPLETED));

            verify(paymentClient, never()).confirm(anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("CANCELED 상태 결제에 재승인 요청하면 PAYMENT_ALREADY_COMPLETED 예외가 발생한다")
        void canceledPaymentRejected() {
            // given
            Payment canceledPayment = PaymentFixture.createPaymentWithStatus(order, PaymentStatus.CANCELED);

            given(orderRepository.findByOrderNumber(ORDER_NUMBER)).willReturn(Optional.of(order));
            given(paymentRepository.findByOrderGroupIdWithLock(ORDER_GROUP_ID))
                .willReturn(List.of(canceledPayment));

            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, ORDER_NUMBER, CORRECT_AMOUNT
            );

            // when & then
            assertThatThrownBy(() -> customerPaymentService.confirm(MEMBER_ID, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.PAYMENT_ALREADY_COMPLETED));
        }

        @Test
        @DisplayName("REFUNDED 상태 결제에 재승인 요청하면 PAYMENT_ALREADY_COMPLETED 예외가 발생한다")
        void refundedPaymentRejected() {
            // given
            Payment refundedPayment = PaymentFixture.createPaymentWithStatus(order, PaymentStatus.REFUNDED);

            given(orderRepository.findByOrderNumber(ORDER_NUMBER)).willReturn(Optional.of(order));
            given(paymentRepository.findByOrderGroupIdWithLock(ORDER_GROUP_ID))
                .willReturn(List.of(refundedPayment));

            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, ORDER_NUMBER, CORRECT_AMOUNT
            );

            // when & then
            assertThatThrownBy(() -> customerPaymentService.confirm(MEMBER_ID, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.PAYMENT_ALREADY_COMPLETED));
        }
    }

    @Nested
    @DisplayName("결제 승인 — 실패: 금액 불일치")
    class PaymentAmountMismatch {

        @Test
        @DisplayName("요청 금액이 그룹 전체 주문 총액과 다르면 PAYMENT_AMOUNT_MISMATCH 예외가 발생한다")
        void amountMismatch() {
            // given
            Long wrongAmount = CORRECT_AMOUNT + 1000L;

            given(orderRepository.findByOrderNumber(ORDER_NUMBER)).willReturn(Optional.of(order));
            given(paymentRepository.findByOrderGroupIdWithLock(ORDER_GROUP_ID))
                .willReturn(List.of(pendingPayment));

            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, ORDER_NUMBER, wrongAmount
            );

            // when & then
            assertThatThrownBy(() -> customerPaymentService.confirm(MEMBER_ID, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.PAYMENT_AMOUNT_MISMATCH));

            verify(paymentClient, never()).confirm(anyString(), anyString(), anyLong());
        }
    }
}
