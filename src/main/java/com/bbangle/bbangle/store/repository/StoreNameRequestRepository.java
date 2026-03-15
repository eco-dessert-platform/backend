package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreNameRequestRepository extends JpaRepository<StoreNameRequest, Long> {

    boolean existsByStatusAndSeller_Id(StoreApprovalStatus status, Long sellerId);
}
