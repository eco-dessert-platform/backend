package com.bbangle.bbangle.claim.repository;

import com.bbangle.bbangle.claim.domain.ClaimDelivery;
import com.bbangle.bbangle.claim.domain.constant.ClaimDeliveryType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimDeliveryRepository extends JpaRepository<ClaimDelivery, Long> {

    Optional<ClaimDelivery> findByClaimIdAndDeliveryType(Long claimId, ClaimDeliveryType deliveryType);

}
