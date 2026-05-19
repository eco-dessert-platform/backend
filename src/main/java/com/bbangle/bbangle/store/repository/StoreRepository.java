package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.store.domain.Store;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreQueryDSLRepository {

    boolean existsByName(String name);

    long countByIdIn(List<Long> ids);
}
