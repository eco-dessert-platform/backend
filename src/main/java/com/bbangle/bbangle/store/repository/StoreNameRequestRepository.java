package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreNameRequestRepository extends
    JpaRepository<StoreNameRequest, Long>, StoreNameRequestQueryDSLRepository {

    @EntityGraph(attributePaths = {"store"})
    Page<StoreNameRequest> findByStatus(StoreApprovalStatus status, Pageable pageable);
}