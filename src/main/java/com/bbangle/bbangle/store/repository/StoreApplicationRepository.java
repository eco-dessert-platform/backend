package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.store.domain.StoreApplication;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreApplicationRepository
    extends JpaRepository<StoreApplication, Long>, StoreApplicationQueryDSLRepository {

    @EntityGraph(attributePaths = "seller")
    List<StoreApplication> findAllWithSellerByIdIn(List<Long> ids);

    List<StoreApplication> findByIdIn(List<Long> ids);

    @Query("select sa from StoreApplication sa " +
        "join fetch sa.seller " +
        "left join fetch sa.store " +
        "where sa.id = :id")
    Optional<StoreApplication> findByIdWithDetails(@Param("id") Long id);
}
