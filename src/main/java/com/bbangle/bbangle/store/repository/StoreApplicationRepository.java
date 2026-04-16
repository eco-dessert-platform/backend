package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreApplicationRepository
    extends JpaRepository<StoreApplication, Long>, StoreApplicationQueryDSLRepository {

    long countByStatus(StoreApprovalStatus status);

    @EntityGraph(attributePaths = "seller")
    List<StoreApplication> findAllWithSellerByIdIn(List<Long> ids);
}
