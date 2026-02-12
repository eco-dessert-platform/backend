package com.bbangle.bbangle.claim.seller.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.claim.domain.CancelRequest;
import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.repository.ClaimRepository;
import com.bbangle.bbangle.claim.seller.controller.dto.ReturnDecisionRequest;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.OrderItemHistory;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[비즈니스 로직] SellerClaimService")
@ExtendWith(MockitoExtension.class)
class SellerClaimServiceUnitTest {

    @InjectMocks
    private SellerClaimService sut;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private OrderItemHistoryRepository orderItemHistoryRepository;

    @Nested
    @DisplayName("반품 decision()")
    class return_decision {

        @Test
        @DisplayName("반품 승인 - 판매자와 반품 요청이 일치하면 approve가 호출되고 OrderItemHistory가 저장된다")
        void decision_approve_success() {
            // given
            Long returnId = 1L;
            Long sellerId = 10L;
            String reason = "승인 사유";
            ReturnRequest returnRequest = mock(ReturnRequest.class);
            OrderItem orderItem = mock(OrderItem.class);

            given(claimRepository.existsClaimRequestBySeller(returnId, sellerId)).willReturn(true);
            given(claimRepository.findById(returnId)).willReturn(Optional.of(returnRequest));
            given(returnRequest.getOrderItem()).willReturn(orderItem);

            // when
            sut.decision(returnId, sellerId, DecisionType.APPROVE, reason);

            // then
            then(returnRequest).should(times(1)).approve(reason);
            then(returnRequest).should(never()).reject(any());
            then(orderItem).should(times(1)).returnApprove();
            then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
        }

        @Test
        @DisplayName("반품 거절 - 판매자와 반품 요청이 일치하면 reject가 호출되고 OrderItemHistory가 저장된다")
        void decision_reject_success() {
            // given
            Long returnId = 1L;
            Long sellerId = 10L;
            String reason = "거절 사유";
            ReturnRequest returnRequest = mock(ReturnRequest.class);
            OrderItem orderItem = mock(OrderItem.class);

            given(claimRepository.existsClaimRequestBySeller(returnId, sellerId)).willReturn(true);
            given(claimRepository.findById(returnId)).willReturn(Optional.of(returnRequest));
            given(returnRequest.getOrderItem()).willReturn(orderItem);

            // when
            sut.decision(returnId, sellerId, DecisionType.REJECT, reason);

            // then
            then(returnRequest).should(times(1)).reject(reason);
            then(returnRequest).should(never()).approve(any());
            then(orderItem).should(times(1)).returnReject();
            then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
        }
    }

    @Nested
    @DisplayName("취소 decision()")
    class cancel_decision {

        @Test
        @DisplayName("취소 승인 - 판매자와 취소 요청이 일치하면 approve가 호출되고 OrderItemHistory가 저장된다")
        void decision_cancel_approve_success() {
            // given
            Long cancelId = 1L;
            Long sellerId = 10L;
            String reason = "취소 승인 사유";

            CancelRequest cancelRequest = mock(CancelRequest.class);
            OrderItem orderItem = mock(OrderItem.class);

            given(claimRepository.existsClaimRequestBySeller(cancelId, sellerId)).willReturn(true);
            given(claimRepository.findById(cancelId)).willReturn(Optional.of(cancelRequest));
            given(cancelRequest.getOrderItem()).willReturn(orderItem);

            // when
            sut.decision(cancelId, sellerId, DecisionType.APPROVE, reason);

            // then
            then(cancelRequest).should(times(1)).approve(reason);
            then(cancelRequest).should(never()).reject(any());
            then(orderItem).should(times(1)).cancelApprove();
            then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
        }

        @Test
        @DisplayName("취소 거절 - 판매자와 취소 요청이 일치하면 reject가 호출되고 OrderItemHistory가 저장된다")
        void decision_cancel_reject_success() {
            // given
            Long cancelId = 1L;
            Long sellerId = 10L;
            String reason = "취소 거절 사유";
            CancelRequest cancelRequest = mock(CancelRequest.class);
            OrderItem orderItem = mock(OrderItem.class);

            given(claimRepository.existsClaimRequestBySeller(cancelId, sellerId)).willReturn(true);
            given(claimRepository.findById(cancelId)).willReturn(Optional.of(cancelRequest));
            given(cancelRequest.getOrderItem()).willReturn(orderItem);

            // when
            sut.decision(cancelId, sellerId, DecisionType.REJECT, reason);

            // then
            then(cancelRequest).should(times(1)).reject(reason);
            then(cancelRequest).should(never()).approve(any());
            then(orderItem).should(times(1)).cancelReject();
            then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
        }
    }

    @Nested
    @DisplayName("공통 예외")
    class common_exception {

        @Test
        @DisplayName("판매자와 요청이 매칭되지 않으면 예외가 발생한다")
        void decision_sellerMismatch_throwException() {
            // given
            Long returnId = 1L;
            Long sellerId = 10L;

            ReturnDecisionRequest request = new ReturnDecisionRequest(DecisionType.APPROVE, "사유");

            given(claimRepository.existsClaimRequestBySeller(returnId, sellerId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> sut.decision(returnId, sellerId, DecisionType.APPROVE, "사유"))
                .isInstanceOf(BbangleException.class)
                .hasMessageContaining(BbangleErrorCode.SELLER_CLAIM_MISMATCH.getMessage());

            then(claimRepository).should(never()).findById(any());
        }

        @Test
        @DisplayName("요청이 존재하지 않으면 예외가 발생한다")
        void decision_claimNotFound_throwException() {
            // given
            Long returnId = 1L;
            Long sellerId = 10L;

            given(claimRepository.existsClaimRequestBySeller(returnId, sellerId)).willReturn(true);
            given(claimRepository.findById(returnId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sut.decision(returnId, sellerId, DecisionType.APPROVE, "사유"))
                .isInstanceOf(BbangleException.class)
                .hasMessageContaining(BbangleErrorCode.CLAIM_NOT_FOUND.getMessage());
        }
    }
}
