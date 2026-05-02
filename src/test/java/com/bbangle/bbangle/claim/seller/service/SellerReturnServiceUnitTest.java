package com.bbangle.bbangle.claim.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.claim.domain.ClaimDelivery;
import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.domain.constant.ReturnRequestRequestStatus;
import com.bbangle.bbangle.claim.repository.ClaimDeliveryRepository;
import com.bbangle.bbangle.claim.repository.ClaimRepository;
import com.bbangle.bbangle.claim.repository.ReturnRequestRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.claim.ReturnRequestFixture;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.OrderItemHistory;
import com.bbangle.bbangle.order.domain.model.CourierCompany;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위 테스트] SellerReturnService")
@ExtendWith(MockitoExtension.class)
class SellerReturnServiceUnitTest {

    @InjectMocks
    private SellerReturnService sut;

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @Mock
    private ClaimDeliveryRepository claimDeliveryRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private OrderItemHistoryRepository orderItemHistoryRepository;

    @Nested
    @DisplayName("registerReturnInvoice()")
    class RegisterReturnInvoiceTests {

        private static final Long RETURN_ID = 1L;
        private static final Long SELLER_ID = 10L;
        private static final CourierCompany COURIER = CourierCompany.CJ_LOGISTICS;
        private static final String TRACKING_NUMBER = "1234567890";

        @Test
        @DisplayName("APPROVED 상태의 클레임에 운송장을 입력하면 PICKUP_SCHEDULED로 변경되고 ClaimDelivery가 저장된다")
        void registerReturnInvoice_success() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_APPROVED);
            ReturnRequest returnRequest = ReturnRequestFixture.approved(orderItem);

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findWithLockById(RETURN_ID)).willReturn(Optional.of(returnRequest));
            given(claimDeliveryRepository.save(any(ClaimDelivery.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            sut.registerReturnInvoice(RETURN_ID, SELLER_ID, COURIER, TRACKING_NUMBER);

            // then
            assertThat(returnRequest.getStatus()).isEqualTo(ReturnRequestRequestStatus.PICKUP_SCHEDULED);
            then(claimDeliveryRepository).should(times(1)).save(any(ClaimDelivery.class));
        }

        @Test
        @DisplayName("셀러 소유권 검증 실패 시 SELLER_CLAIM_MISMATCH 예외가 발생하고 이후 로직은 실행되지 않는다")
        void registerReturnInvoice_fails_when_seller_mismatch() {
            // given
            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> sut.registerReturnInvoice(RETURN_ID, SELLER_ID, COURIER, TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.SELLER_CLAIM_MISMATCH);

            then(returnRequestRepository).should(never()).findWithLockById(any());
            then(claimDeliveryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 claimId로 요청하면 CLAIM_NOT_FOUND 예외가 발생한다")
        void registerReturnInvoice_fails_when_claim_not_found() {
            // given
            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findWithLockById(RETURN_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.registerReturnInvoice(RETURN_ID, SELLER_ID, COURIER, TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_NOT_FOUND);

            then(claimDeliveryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("APPROVED가 아닌 상태의 클레임에 운송장을 입력하면 CLAIM_INVALID_STATUS 예외가 발생한다")
        void registerReturnInvoice_fails_when_invalid_status() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.requested(orderItem);  // REQUESTED 상태

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findWithLockById(RETURN_ID)).willReturn(Optional.of(returnRequest));

            // when & then
            assertThatThrownBy(() -> sut.registerReturnInvoice(RETURN_ID, SELLER_ID, COURIER, TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            then(claimDeliveryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 PICKUP_SCHEDULED 상태면 운송장 중복 입력이 차단된다")
        void registerReturnInvoice_fails_when_already_pickup_scheduled() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REQUESTED);
            ReturnRequest returnRequest = ReturnRequestFixture.pickupScheduled(orderItem);

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findWithLockById(RETURN_ID)).willReturn(Optional.of(returnRequest));

            // when & then
            assertThatThrownBy(() -> sut.registerReturnInvoice(RETURN_ID, SELLER_ID, COURIER, TRACKING_NUMBER))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);
        }
    }

    @Nested
    @DisplayName("decision()")
    class DecisionTests {

        @Test
        @DisplayName("반품 승인 - 여러 반품을 한 번에 승인할 수 있다")
        void decision_approve_success() {
            // given
            List<Long> returnIds = List.of(1L, 2L, 3L);
            Long sellerId = 10L;
            String reason = "승인 사유";

            OrderItem orderItem1 = OrderItemFixture.orderReturnRequested();
            OrderItem orderItem2 = OrderItemFixture.orderReturnRequested();
            OrderItem orderItem3 = OrderItemFixture.orderReturnRequested();
            ReturnRequest returnRequest1 = ReturnRequestFixture.requested(orderItem1);
            ReturnRequest returnRequest2 = ReturnRequestFixture.requested(orderItem2);
            ReturnRequest returnRequest3 = ReturnRequestFixture.requested(orderItem3);

            given(returnRequestRepository.countReturnsBySeller(returnIds, sellerId)).willReturn(3L);
            given(returnRequestRepository.findAllById(returnIds))
                .willReturn(List.of(returnRequest1, returnRequest2, returnRequest3));

            // when
            sut.decision(returnIds, sellerId, DecisionType.APPROVE, reason);

            // then
            then(returnRequestRepository).should(times(1)).countReturnsBySeller(returnIds, sellerId);
            then(returnRequestRepository).should(times(1)).findAllById(returnIds);
            then(orderItemHistoryRepository).should(times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("반품 거절 - 여러 반품을 한 번에 거절할 수 있다")
        void decision_reject_success() {
            // given
            List<Long> returnIds = List.of(1L, 2L);
            Long sellerId = 10L;
            String reason = "거절 사유";
            OrderItem orderItem1 = OrderItemFixture.orderReturnRequested();
            OrderItem orderItem2 = OrderItemFixture.orderReturnRequested();
            ReturnRequest returnRequest1 = ReturnRequestFixture.requested(orderItem1);
            ReturnRequest returnRequest2 = ReturnRequestFixture.requested(orderItem2);

            given(returnRequestRepository.countReturnsBySeller(returnIds, sellerId)).willReturn(2L);
            given(returnRequestRepository.findAllById(returnIds))
                .willReturn(List.of(returnRequest1, returnRequest2));

            // when
            sut.decision(returnIds, sellerId, DecisionType.REJECT, reason);

            // then
            then(returnRequestRepository).should(times(1)).countReturnsBySeller(returnIds, sellerId);
            then(returnRequestRepository).should(times(1)).findAllById(returnIds);
            then(orderItemHistoryRepository).should(times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("판매자 권한 검증 실패 - 판매자가 소유하지 않은 반품은 수정할 수 없다")
        void decision_fails_when_seller_mismatch() {
            // given
            List<Long> returnIds = List.of(1L, 2L, 3L);
            Long sellerId = 10L;
            String reason = "사유";

            given(returnRequestRepository.countReturnsBySeller(returnIds, sellerId)).willReturn(0L);

            // when & then
            assertThatThrownBy(
                () -> sut.decision(returnIds, sellerId, DecisionType.APPROVE, reason)
            ).isInstanceOf(BbangleException.class);

            then(returnRequestRepository).should(never()).findAllById(anyList());
            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("부분 일치 실패 - 일부 반품만 판매자 소유일 때 예외가 발생한다")
        void decision_fails_when_partial_match() {
            // given
            List<Long> returnIds = List.of(1L, 2L, 3L, 4L, 5L);  // 5개 요청
            Long sellerId = 10L;
            String reason = "사유";

            given(returnRequestRepository.countReturnsBySeller(returnIds, sellerId)).willReturn(3L);  // 3개만 소유

            // when & then
            assertThatThrownBy(
                () -> sut.decision(returnIds, sellerId, DecisionType.APPROVE, reason)
            ).isInstanceOf(BbangleException.class);

            then(returnRequestRepository).should(never()).findAllById(anyList());
            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("null 반환 처리 - countReturnsBySeller가 null을 반환하면 예외가 발생한다")
        void decision_fails_when_count_returns_null() {
            // given
            List<Long> returnIds = List.of(1L);
            Long sellerId = 10L;
            String reason = "사유";

            given(returnRequestRepository.countReturnsBySeller(returnIds, sellerId)).willReturn(null);

            // when & then
            assertThatThrownBy(
                () -> sut.decision(returnIds, sellerId, DecisionType.APPROVE, reason)
            ).isInstanceOf(BbangleException.class);

            then(orderItemHistoryRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("refuseReturn()")
    class RefuseReturnTests {

        private static final Long RETURN_ID = 1L;
        private static final Long SELLER_ID = 10L;
        private static final String REFUSAL_REASON = "반품 불가 상품입니다.";

        @Test
        @DisplayName("REQUESTED 상태의 반품을 거절하면 상태가 REJECTED로 변경되고 거절 사유와 히스토리가 저장된다")
        void refuseReturn_fromRequested_success() {
            // given
            OrderItem orderItem = OrderItemFixture.orderReturnRequested();
            ReturnRequest returnRequest = ReturnRequestFixture.requested(orderItem);

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findWithLockById(RETURN_ID)).willReturn(Optional.of(returnRequest));

            // when
            sut.refuseReturn(RETURN_ID, SELLER_ID, REFUSAL_REASON);

            // then
            assertThat(returnRequest.getStatus()).isEqualTo(ReturnRequestRequestStatus.REJECTED);
            assertThat(returnRequest.getSellerComment()).isEqualTo(REFUSAL_REASON);
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REJECTED);
            then(returnRequestRepository).should(times(1)).findWithLockById(RETURN_ID);
            then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
        }

        @Test
        @DisplayName("PICKUP_SCHEDULED 상태의 반품을 거절하면 상태가 REJECTED로 변경되고 히스토리가 저장된다")
        void refuseReturn_fromPickupScheduled_success() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_APPROVED);
            ReturnRequest returnRequest = ReturnRequestFixture.pickupScheduled(orderItem);

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findWithLockById(RETURN_ID)).willReturn(Optional.of(returnRequest));

            // when
            sut.refuseReturn(RETURN_ID, SELLER_ID, REFUSAL_REASON);

            // then
            assertThat(returnRequest.getStatus()).isEqualTo(ReturnRequestRequestStatus.REJECTED);
            assertThat(returnRequest.getSellerComment()).isEqualTo(REFUSAL_REASON);
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_REJECTED);
            then(returnRequestRepository).should(times(1)).findWithLockById(RETURN_ID);
            then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
        }

        @Test
        @DisplayName("셀러 소유권 검증 실패 시 SELLER_CLAIM_MISMATCH 예외가 발생하고 이후 로직은 실행되지 않는다")
        void refuseReturn_fails_when_seller_mismatch() {
            // given
            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> sut.refuseReturn(RETURN_ID, SELLER_ID, REFUSAL_REASON))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.SELLER_CLAIM_MISMATCH);

            then(returnRequestRepository).should(never()).findWithLockById(any());
            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 returnId로 요청하면 CLAIM_NOT_FOUND 예외가 발생한다")
        void refuseReturn_fails_when_claim_not_found() {
            // given
            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findWithLockById(RETURN_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.refuseReturn(RETURN_ID, SELLER_ID, REFUSAL_REASON))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_NOT_FOUND);

            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("COMPLETED 상태의 반품을 거절하려 하면 CLAIM_INVALID_STATUS 예외가 발생한다")
        void refuseReturn_fails_when_already_completed() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_COMPLETED);
            ReturnRequest returnRequest = ReturnRequestFixture.withStatus(
                ReturnRequestRequestStatus.COMPLETED, orderItem
            );

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findWithLockById(RETURN_ID)).willReturn(Optional.of(returnRequest));

            // when & then
            assertThatThrownBy(() -> sut.refuseReturn(RETURN_ID, SELLER_ID, REFUSAL_REASON))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("이미 REJECTED 상태인 반품을 다시 거절하려 하면 CLAIM_INVALID_STATUS 예외가 발생한다")
        void refuseReturn_fails_when_already_rejected() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_REJECTED);
            ReturnRequest returnRequest = ReturnRequestFixture.rejected(orderItem);

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findWithLockById(RETURN_ID)).willReturn(Optional.of(returnRequest));

            // when & then
            assertThatThrownBy(() -> sut.refuseReturn(RETURN_ID, SELLER_ID, REFUSAL_REASON))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            then(orderItemHistoryRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("processReturn()")
    class ProcessReturnTests {

        private static final Long RETURN_ID = 1L;
        private static final Long SELLER_ID = 10L;

        @Test
        @DisplayName("INSPECTION_APPROVED 상태의 반품을 처리하면 COMPLETED로 변경되고 OrderItem이 RETURN_COMPLETED가 된다")
        void processReturn_success() {
            // given
            OrderItem orderItem = OrderItemFixture.orderItemWithStatus(OrderStatus.RETURN_APPROVED);
            ReturnRequest returnRequest = ReturnRequestFixture.withStatus(
                ReturnRequestRequestStatus.INSPECTION_APPROVED, orderItem
            );

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findWithLockById(RETURN_ID)).willReturn(Optional.of(returnRequest));

            // when
            sut.processReturn(RETURN_ID, SELLER_ID);

            // then
            assertThat(returnRequest.getStatus()).isEqualTo(ReturnRequestRequestStatus.COMPLETED);
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.RETURN_COMPLETED);
            then(returnRequestRepository).should(times(1)).findWithLockById(RETURN_ID);
            then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
        }

        @Test
        @DisplayName("셀러 소유권 검증 실패 시 SELLER_CLAIM_MISMATCH 예외가 발생하고 이후 로직은 실행되지 않는다")
        void processReturn_fails_when_seller_mismatch() {
            // given
            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> sut.processReturn(RETURN_ID, SELLER_ID))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.SELLER_CLAIM_MISMATCH);

            then(returnRequestRepository).should(never()).findWithLockById(any());
            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 returnId로 요청하면 CLAIM_NOT_FOUND 예외가 발생한다")
        void processReturn_fails_when_claim_not_found() {
            // given
            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findWithLockById(RETURN_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.processReturn(RETURN_ID, SELLER_ID))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_NOT_FOUND);

            then(orderItemHistoryRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("REQUESTED 상태의 반품을 최종 처리하려 하면 CLAIM_INVALID_STATUS 예외가 발생한다")
        void processReturn_fails_when_invalid_status() {
            // given
            OrderItem orderItem = OrderItemFixture.orderReturnRequested();
            ReturnRequest returnRequest = ReturnRequestFixture.requested(orderItem);

            given(claimRepository.existsClaimRequestBySeller(RETURN_ID, SELLER_ID)).willReturn(true);
            given(returnRequestRepository.findWithLockById(RETURN_ID)).willReturn(Optional.of(returnRequest));

            // when & then
            assertThatThrownBy(() -> sut.processReturn(RETURN_ID, SELLER_ID))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.CLAIM_INVALID_STATUS);

            then(orderItemHistoryRepository).should(never()).save(any());
        }
    }
}
