package com.bbangle.bbangle.claim.repository.custom;

public interface ClaimCustomRepository {

    boolean existsClaimRequestBySeller(Long claimId, Long sellerId);

}
