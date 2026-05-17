package com.bbangle.bbangle.claim.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.claim.domain.ClaimDelivery;
import com.bbangle.bbangle.claim.domain.ExchangeRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.domain.constant.ExchangeRequestStatus;
import com.bbangle.bbangle.claim.repository.ClaimDeliveryRepository;
import com.bbangle.bbangle.claim.repository.ClaimRepository;
import com.bbangle.bbangle.claim.repository.ExchangeRequestRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.claim.ExchangeRequestFixture;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.OrderItemHistory;
import com.bbangle.bbangle.order.domain.model.CourierCompany;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위 테스트] SellerExchangeService - processExchange()")
@ExtendWith(MockitoExtension.class)
class SellerExchangeServiceProcessTest {

    @InjectMocks
    private SellerExchangeService sut;

    @Mock
    private ExchangeRequestRepository exchangeRequestRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimDeliveryRepository claimDeliveryRepository;

    @Mock
    private OrderItemHistoryRepository orderItemHistoryRepository;

    @Nested
    @DisplayName("APPROVE 시나리오")
    class ApproveTests {

        private static final Long EXCHANGE_ID = 1L;
        private static final Long SELLER_ID = 10L;
        private static final String REASON = "교환 승인";

        @Test
        @DisplayName("성공 - REQUESTED 상태의 교환 건에 대해 승인 처리 시 상태가 APPROVED로 전이되고 OrderItemHistory가 저장된다")
        void processExchange_approve_success() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.EXCHANGE_REQUEST);
            ExchangeRequest exchangeRequest = ExchangeRequestFixture.requested(orderItem);

            given(claimRepository.existsClaimRequestBySeller(EXCHANGE_ID, SELLER_ID)).willReturn(true);
            given(exchangeRequestRepository.findWithLockById(EXCHANGE_ID)).willReturn(Optional.of(exchangeRequest));

            // when
            sut.processExchange(EXCHANGE_ID, SELLER_ID, DecisionType.APPROVE, REASON);

            // then
            assertThat(exchangeRequest.getStatus()).isEqualTo(ExchangeRequestStatus.APPROVED);
            assertThat(exchangeRequest.getSellerComment()).isEqualTo(REASON);
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.EXCHANGE_APPROVED);
            then(exchangeRequestRepository).should(times(1)).findWithLockById(EXCHANGE_ID);
            then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
        }

        @Test
        @DisplayName("실패 (권한) - 타 판매자의 교환 건에 접근 시 SELLER_CLAIM_MISMATCH 예외가 발생하고 이후 로직은 실행되지 않는다")
        void processExchange_approve_fails_when_seller_mismatch() {
            // given
            given(claimRepository.existsClaimRequestBySeller(EXCHANGE_ID, SELLER_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> sut.processExchange(EXCHANGE_ID, SELLER_ID, DecisionType.APPROVE, REASON))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.SELLER_CLAIM_MISMATCH);

            then(exchangeRequestRepository).should(never()).findWithLockById(any());
            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("실패 (상태) - 이미 APPROVED 처리된 교환 건에 대해 승인 시도 시 CLAIM_INVALID_STATUS 예외가 발생한다")
        void processExchange_approve_fails_when_already_approved() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.EXCHANGE_APPROVED);
            ExchangeRequest exchangeRequest = ExchangeRequestFixture.approved(orderItem);

            given(claimRepository.existsClaimRequestBySeller(EXCHANGE_ID, SELLER_ID)).willReturn(true);
            given(exchangeRequestRepository.findWithLockById(EXCHANGE_ID)).willReturn(Optional.of(exchangeRequest));

            // when & then
            assertThatThrownBy(() -> sut.processExchange(EXCHANGE_ID, SELLER_ID, DecisionType.APPROVE, REASON))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("실패 (상태) - 이미 REJECTED 처리된 교환 건에 대해 승인 시도 시 CLAIM_INVALID_STATUS 예외가 발생한다")
        void processExchange_approve_fails_when_already_rejected() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.EXCHANGE_REJECTED);
            ExchangeRequest exchangeRequest = ExchangeRequestFixture.rejected(orderItem);

            given(claimRepository.existsClaimRequestBySeller(EXCHANGE_ID, SELLER_ID)).willReturn(true);
            given(exchangeRequestRepository.findWithLockById(EXCHANGE_ID)).willReturn(Optional.of(exchangeRequest));

            // when & then
            assertThatThrownBy(() -> sut.processExchange(EXCHANGE_ID, SELLER_ID, DecisionType.APPROVE, REASON))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            then(orderItemHistoryRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("REJECT 시나리오")
    class RejectTests {

        private static final Long EXCHANGE_ID = 2L;
        private static final Long SELLER_ID = 10L;
        private static final String REASON = "교환 거절 사유";

        @Test
        @DisplayName("성공 - REQUESTED 상태의 교환 건에 대해 거절 처리 시 상태가 REJECTED로 전이되고 OrderItemHistory가 저장된다")
        void processExchange_reject_success() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.EXCHANGE_REQUEST);
            ExchangeRequest exchangeRequest = ExchangeRequestFixture.requested(orderItem);

            given(claimRepository.existsClaimRequestBySeller(EXCHANGE_ID, SELLER_ID)).willReturn(true);
            given(exchangeRequestRepository.findWithLockById(EXCHANGE_ID)).willReturn(Optional.of(exchangeRequest));

            // when
            sut.processExchange(EXCHANGE_ID, SELLER_ID, DecisionType.REJECT, REASON);

            // then
            assertThat(exchangeRequest.getStatus()).isEqualTo(ExchangeRequestStatus.REJECTED);
            assertThat(exchangeRequest.getSellerComment()).isEqualTo(REASON);
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.EXCHANGE_REJECTED);
            then(exchangeRequestRepository).should(times(1)).findWithLockById(EXCHANGE_ID);
            then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
        }

        @Test
        @DisplayName("실패 - 이미 APPROVED 처리된 교환 건에 대해 거절 시도 시 CLAIM_INVALID_STATUS 예외가 발생한다")
        void processExchange_reject_fails_when_already_approved() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.EXCHANGE_APPROVED);
            ExchangeRequest exchangeRequest = ExchangeRequestFixture.approved(orderItem);

            given(claimRepository.existsClaimRequestBySeller(EXCHANGE_ID, SELLER_ID)).willReturn(true);
            given(exchangeRequestRepository.findWithLockById(EXCHANGE_ID)).willReturn(Optional.of(exchangeRequest));

            // when & then
            assertThatThrownBy(() -> sut.processExchange(EXCHANGE_ID, SELLER_ID, DecisionType.REJECT, REASON))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 exchangeId로 요청하면 CLAIM_NOT_FOUND 예외가 발생한다")
        void processExchange_reject_fails_when_claim_not_found() {
            // given
            given(claimRepository.existsClaimRequestBySeller(EXCHANGE_ID, SELLER_ID)).willReturn(true);
            given(exchangeRequestRepository.findWithLockById(EXCHANGE_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.processExchange(EXCHANGE_ID, SELLER_ID, DecisionType.REJECT, REASON))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_NOT_FOUND);

            then(orderItemHistoryRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("registerExchangeInvoice() - 교환 재배송 운송장 입력")
    class RegisterExchangeInvoiceTests {

        private static final Long EXCHANGE_ID = 3L;
        private static final Long SELLER_ID = 10L;
        private static final CourierCompany COURIER = CourierCompany.CJ_LOGISTICS;
        private static final String TRACKING_NUMBER = "1234567890";

        @Test
        @DisplayName("성공 - APPROVED 상태의 교환 건에 운송장을 입력하면 상태가 RESHIPPED/EXCHANGE_ITEM_SHIPPED로 전이되고 ClaimDelivery와 OrderItemHistory가 저장된다")
        void registerExchangeInvoice_success() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.EXCHANGE_APPROVED);
            ExchangeRequest exchangeRequest = ExchangeRequestFixture.approved(orderItem);

            given(claimRepository.existsClaimRequestBySeller(EXCHANGE_ID, SELLER_ID)).willReturn(true);
            given(exchangeRequestRepository.findWithLockById(EXCHANGE_ID)).willReturn(Optional.of(exchangeRequest));
            given(claimDeliveryRepository.save(any(ClaimDelivery.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            sut.registerExchangeInvoice(EXCHANGE_ID, SELLER_ID, COURIER, TRACKING_NUMBER);

            // then
            assertThat(exchangeRequest.getStatus()).isEqualTo(ExchangeRequestStatus.RESHIPPED);
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.EXCHANGE_ITEM_SHIPPED);
            then(exchangeRequestRepository).should(times(1)).findWithLockById(EXCHANGE_ID);
            then(claimDeliveryRepository).should(times(1)).save(any(ClaimDelivery.class));
            then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
        }

        @Test
        @DisplayName("실패 (권한) - 타 판매자의 교환 건에 운송장을 입력하려 하면 SELLER_CLAIM_MISMATCH 예외가 발생하고 이후 로직은 실행되지 않는다")
        void registerExchangeInvoice_fails_when_seller_mismatch() {
            // given
            given(claimRepository.existsClaimRequestBySeller(EXCHANGE_ID, SELLER_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> sut.registerExchangeInvoice(EXCHANGE_ID, SELLER_ID, COURIER, TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.SELLER_CLAIM_MISMATCH);

            then(exchangeRequestRepository).should(never()).findWithLockById(any());
            then(claimDeliveryRepository).should(never()).save(any());
            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("실패 (상태) - REQUESTED 상태의 교환 건(미승인)에 운송장을 입력하려 하면 CLAIM_INVALID_STATUS 예외가 발생한다")
        void registerExchangeInvoice_fails_when_not_approved() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.EXCHANGE_REQUEST);
            ExchangeRequest exchangeRequest = ExchangeRequestFixture.requested(orderItem);

            given(claimRepository.existsClaimRequestBySeller(EXCHANGE_ID, SELLER_ID)).willReturn(true);
            given(exchangeRequestRepository.findWithLockById(EXCHANGE_ID)).willReturn(Optional.of(exchangeRequest));

            // when & then
            assertThatThrownBy(() -> sut.registerExchangeInvoice(EXCHANGE_ID, SELLER_ID, COURIER, TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            then(claimDeliveryRepository).should(never()).save(any());
            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("실패 (상태) - 이미 RESHIPPED(운송장 등록 완료) 상태인 교환 건에 중복 입력하려 하면 CLAIM_INVALID_STATUS 예외가 발생한다")
        void registerExchangeInvoice_fails_when_already_reshipped() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.EXCHANGE_ITEM_SHIPPED);
            ExchangeRequest exchangeRequest = ExchangeRequestFixture.withStatus(ExchangeRequestStatus.RESHIPPED, orderItem);

            given(claimRepository.existsClaimRequestBySeller(EXCHANGE_ID, SELLER_ID)).willReturn(true);
            given(exchangeRequestRepository.findWithLockById(EXCHANGE_ID)).willReturn(Optional.of(exchangeRequest));

            // when & then
            assertThatThrownBy(() -> sut.registerExchangeInvoice(EXCHANGE_ID, SELLER_ID, COURIER, TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            then(claimDeliveryRepository).should(never()).save(any());
            then(orderItemHistoryRepository).should(never()).save(any());
        }
    }
}
