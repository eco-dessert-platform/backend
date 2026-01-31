package com.bbangle.bbangle.claim.seller.service;

import com.bbangle.bbangle.claim.domain.Claim;
import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import com.bbangle.bbangle.claim.repository.ClaimRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SellerClaimService {

    private final ClaimRepository claimRepository;

    @Transactional
    public void decision(Long returnId, Long sellerId, DecisionType decisionType, String reason) {
        if (!claimRepository.existsReturnRequestBySeller(returnId, sellerId)) {
            throw new BbangleException(BbangleErrorCode.SELLER_CLAIM_MISMATCH);
        }

        Claim claim = claimRepository.findById(returnId)
            .orElseThrow(() -> new BbangleException(BbangleErrorCode.CLAIM_NOT_FOUND));

        if (claim instanceof ReturnRequest returnRequest) {
            switch (decisionType) {
                case APPROVE -> returnRequest.approve(reason);
                case REJECT -> returnRequest.reject(reason);
            }
            return;
        }

        throw new BbangleException(BbangleErrorCode.CLAIM_NOT_FOUND);
    }

}
