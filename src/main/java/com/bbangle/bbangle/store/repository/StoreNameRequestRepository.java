package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.store.domain.StoreNameRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreNameRequestRepository extends
    JpaRepository<StoreNameRequest, Long>, StoreNameRequestQueryDSLRepository {
}