package com.bbangle.bbangle.store.repository;

import com.bbangle.bbangle.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreQueryDSLRepository {

}
