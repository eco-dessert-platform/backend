package com.bbangle.bbangle.seller.repository;

import com.bbangle.bbangle.seller.domain.Seller;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    @Query("select s.store.id from Seller s where s.id = :sellerId")
    Long findStoreIdBySellerId(@Param("sellerId") Long sellerId);

    @Query("select s from Seller s join fetch s.store where s.id = :sellerId")
    Optional<Seller> findByIdWithStore(@Param("sellerId") Long sellerId);
}
