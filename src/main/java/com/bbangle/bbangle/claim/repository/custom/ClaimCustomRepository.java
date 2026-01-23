package com.bbangle.bbangle.claim.repository.custom;

public interface ClaimCustomRepository {

    boolean existsReturnRequestBySeller(Long claimId, Long sellerId);
    
}
