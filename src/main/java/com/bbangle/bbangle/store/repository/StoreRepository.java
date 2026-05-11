package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.store.domain.Store;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreQueryDSLRepository {

    boolean existsByName(String name);

    Page<Store> findByIsDeletedFalse(Pageable pageable);

    long countByIdInAndIsDeletedFalse(List<Long> ids);

    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Store s
        SET s.isDeleted = true
        WHERE s.id IN :storeIds
        """)
    void softDeleteByIds(@Param("storeIds") List<Long> storeIds);
}
