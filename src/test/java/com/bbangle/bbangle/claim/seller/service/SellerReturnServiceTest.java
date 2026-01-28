package com.bbangle.bbangle.claim.seller.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.repository.ClaimRepository;
import com.bbangle.bbangle.claim.seller.controller.dto.ReturnDecisionRequest;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[비즈니스 로직] SellerReturnService")
@ExtendWith(MockitoExtension.class)
class SellerReturnServiceTest {

    @InjectMocks
    private SellerReturnService sut;

    @Mock
    private ClaimRepository claimRepository;

    @Test
    @DisplayName("반품 승인 - 판매자와 반품 요청이 일치하면 approve가 호출된다")
    void returnDecision_approve_success() {
        // given
        Long returnId = 1L;
        Long sellerId = 10L;
        String reason = "승인 사유";

        ReturnDecisionRequest request = new ReturnDecisionRequest(DecisionType.APPROVE, reason);
        ReturnRequest returnRequest = mock(ReturnRequest.class);

        given(claimRepository.existsReturnRequestBySeller(returnId, sellerId)).willReturn(true);
        given(claimRepository.findById(returnId)).willReturn(Optional.of(returnRequest));

        // when
        sut.returnDecision(returnId, sellerId, request);

        // then
        then(returnRequest).should(times(1)).approve(reason);
        then(returnRequest).should(never()).reject(any());
    }

    @Test
    @DisplayName("반품 거절 - 판매자와 반품 요청이 일치하면 reject가 호출된다")
    void returnDecision_reject_success() {
        // given
        Long returnId = 1L;
        Long sellerId = 10L;
        String reason = "거절 사유";
        ReturnDecisionRequest request = new ReturnDecisionRequest(DecisionType.REJECT, reason);
        ReturnRequest returnRequest = mock(ReturnRequest.class);

        given(claimRepository.existsReturnRequestBySeller(returnId, sellerId)).willReturn(true);
        given(claimRepository.findById(returnId)).willReturn(Optional.of(returnRequest));

        // when
        sut.returnDecision(returnId, sellerId, request);

        // then
        then(returnRequest).should(times(1)).reject(reason);
        then(returnRequest).should(never()).approve(any());
    }

    @Test
    @DisplayName("판매자와 반품 요청이 매칭되지 않으면 예외가 발생한다")
    void returnDecision_sellerMismatch_throwException() {
        // given
        Long returnId = 1L;
        Long sellerId = 10L;

        ReturnDecisionRequest request = new ReturnDecisionRequest(DecisionType.APPROVE, "사유");

        given(claimRepository.existsReturnRequestBySeller(returnId, sellerId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> sut.returnDecision(returnId, sellerId, request))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_CLAIM_MISMATCH.getMessage());

        then(claimRepository).should(never()).findById(any());
    }

    @Test
    @DisplayName("반품 요청이 존재하지 않으면 예외가 발생한다")
    void returnDecision_claimNotFound_throwException() {
        // given
        Long returnId = 1L;
        Long sellerId = 10L;

        ReturnDecisionRequest request = new ReturnDecisionRequest(DecisionType.APPROVE, "사유");

        given(claimRepository.existsReturnRequestBySeller(returnId, sellerId)).willReturn(true);
        given(claimRepository.findById(returnId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> sut.returnDecision(returnId, sellerId, request))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.CLAIM_NOT_FOUND.getMessage());
    }
}