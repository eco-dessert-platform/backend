package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import java.util.Optional;

public interface StoreNameRequestQueryDSLRepository {

    Optional<StoreApprovalStatus> findActiveRequestsBySellerId(Long sellerId);
}
