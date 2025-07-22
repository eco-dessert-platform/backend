package com.bbangle.bbangle.board.repository;

import com.bbangle.bbangle.board.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long>, StoreQueryDSLRepository {

}
