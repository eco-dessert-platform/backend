package com.bbangle.bbangle.payment.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.order.domain.OrderFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.payment.customer.dto.request.PaymentConfirmRequest;
import com.bbangle.bbangle.payment.customer.dto.response.PaymentConfirmResponse;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.payment.domain.PaymentMethod;
import com.bbangle.bbangle.payment.domain.PaymentStatus;
import com.bbangle.bbangle.payment.repository.PaymentRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합 테스트] CustomerPaymentService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerPaymentServiceIntegrationTest {

    @Autowired
    private CustomerPaymentService customerPaymentService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager em;

    private static final String ORDER_NUMBER = "ORD-PAYMENT-TEST-001";
    private static final String ORDER_GROUP_ID = "paytest001grp0000000000000001";
    private static final String PAYMENT_KEY = "toss_stub_key_abc123";
    private static final Long CORRECT_AMOUNT = 50000L;

    private Member member;
    private Order order;
    private Payment pendingPayment;

    @BeforeEach
    void setUp() {
        // 회원 저장
        member = memberRepository.save(Member.builder()
            .name("결제테스터")
            .email("payment-test@bbangle.com")
            .provider(OauthServerType.KAKAO)
            .providerId("payment-test-kakao-id")
            .build());

        // 주문 저장 (회원 연결, 금액 50000, orderGroupId 포함)
        order = orderRepository.save(
            OrderFixture.createOrderWithAmount(CORRECT_AMOUNT.intValue())
                .toBuilder()
                .orderNumber(ORDER_NUMBER)
                .orderGroupId(ORDER_GROUP_ID)
                .member(member)
                .build()
        );

        // PENDING 상태의 결제 저장
        pendingPayment = paymentRepository.save(
            Payment.create(order, PaymentStatus.PENDING, PaymentMethod.CARD, null)
        );

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("결제 승인 — 성공")
    class ConfirmSuccess {

        @Test
        @DisplayName("올바른 요청이면 Payment 상태가 COMPLETED로 변경되고 paymentKey·paidAt이 저장된다")
        void success() {
            // given
            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, ORDER_NUMBER, CORRECT_AMOUNT
            );

            // when
            PaymentConfirmResponse response = customerPaymentService.confirm(member.getId(), request);

            em.flush();
            em.clear();

            // then — 응답 검증
            assertThat(response.orderNumber()).isEqualTo(ORDER_NUMBER);
            assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.COMPLETED.name());

            // DB 반영 검증
            Payment saved = paymentRepository.findByOrderOrderNumber(ORDER_NUMBER).orElseThrow();
            assertThat(saved.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(saved.getPaymentKey()).isEqualTo(PAYMENT_KEY);
            assertThat(saved.getPaidAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("결제 승인 — 실패")
    class ConfirmFail {

        @Test
        @DisplayName("존재하지 않는 orderNumber이면 ORDER_NOT_FOUND 예외가 발생한다")
        void orderNotFound() {
            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, "WRONG-ORDER-NUMBER", CORRECT_AMOUNT
            );

            assertThatThrownBy(() -> customerPaymentService.confirm(member.getId(), request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.ORDER_NOT_FOUND));
        }

        @Test
        @DisplayName("다른 회원의 주문이면 ORDER_ACCESS_DENIED 예외가 발생한다")
        void accessDenied() {
            Member otherMember = memberRepository.save(Member.builder()
                .name("다른회원")
                .email("other@bbangle.com")
                .provider(OauthServerType.KAKAO)
                .providerId("other-kakao-id")
                .build());

            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, ORDER_NUMBER, CORRECT_AMOUNT
            );

            assertThatThrownBy(() -> customerPaymentService.confirm(otherMember.getId(), request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.ORDER_ACCESS_DENIED));
        }

        @Test
        @DisplayName("이미 COMPLETED인 결제에 재승인 요청하면 PAYMENT_ALREADY_COMPLETED 예외가 발생한다")
        void alreadyCompleted() {
            // PENDING → COMPLETED로 먼저 전환
            Payment payment = paymentRepository.findByOrderOrderNumber(ORDER_NUMBER).orElseThrow();
            payment.confirm(PAYMENT_KEY, java.time.LocalDateTime.now());
            em.flush();
            em.clear();

            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, ORDER_NUMBER, CORRECT_AMOUNT
            );

            assertThatThrownBy(() -> customerPaymentService.confirm(member.getId(), request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.PAYMENT_ALREADY_COMPLETED));
        }

        @Test
        @DisplayName("요청 금액이 주문 총액과 다르면 PAYMENT_AMOUNT_MISMATCH 예외가 발생한다")
        void amountMismatch() {
            PaymentConfirmRequest request = new PaymentConfirmRequest(
                PAYMENT_KEY, ORDER_NUMBER, CORRECT_AMOUNT + 1000L
            );

            assertThatThrownBy(() -> customerPaymentService.confirm(member.getId(), request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                    .isEqualTo(BbangleErrorCode.PAYMENT_AMOUNT_MISMATCH));
        }
    }
}
