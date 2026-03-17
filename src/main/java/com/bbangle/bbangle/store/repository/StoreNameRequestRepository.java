package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// TODO : Test
public interface StoreNameRequestRepository extends JpaRepository<StoreNameRequest, Long> {

    // TODO : 인덱스 추가하기 (seller_id, status)
    @Query("""
    SELECT r FROM StoreNameRequest r
    WHERE r.seller.id = :sellerId
    AND r.status IN :statuses
    ORDER BY CASE r.status
        WHEN 'APPROVE' THEN 1
        WHEN 'PENDING' THEN 2
        ELSE 3
    END
    """)
    List<StoreNameRequest> findActiveRequestsBySellerId(Long sellerId, List<StoreApprovalStatus> statuses, Pageable pageable);
}