package com.bbangle.bbangle.claim.seller.service;

import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.repository.ClaimRepository;
import com.bbangle.bbangle.claim.seller.controller.dto.ReturnDecisionRequest;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SellerReturnService {

    private final ClaimRepository claimRepository;

    @Transactional
    public void returnDecision(Long returnId, Long sellerId, ReturnDecisionRequest returnDecisionRequest) {
        if (!claimRepository.existsReturnRequestBySeller(returnId, sellerId)) {
            throw new BbangleException(BbangleErrorCode.SELLER_CLAIM_MISMATCH);
        }

        ReturnRequest returnRequest = (ReturnRequest) claimRepository.findById(returnId)
            .filter(ReturnRequest.class::isInstance)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.CLAIM_NOT_FOUND));

        switch (returnDecisionRequest.decisionType()) {
            case APPROVE -> returnRequest.approve(returnDecisionRequest.reason());
            case REJECT -> returnRequest.reject(returnDecisionRequest.reason());
        }
    }

}
