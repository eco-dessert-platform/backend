package com.bbangle.bbangle.seller.repository;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.seller.domain.Seller;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SellerRepository extends JpaRepository<Seller, Long> {
  
    @Query("select s.store.id from Seller s where s.id = :sellerId")
    Long findStoreIdBySellerId(@Param("sellerId") Long sellerId);

    @Query("select s from Seller s join fetch s.store where s.id = :sellerId")
    Optional<Seller> findByIdWithStore(@Param("sellerId") Long sellerId);
  
    Optional<Seller> findByProviderAndProviderId(OauthServerType provider, String providerId);

    boolean existsByStore_Id(Long storeId);

    @Query("SELECT se FROM Seller se WHERE se.store.id IN :storeIds")
    List<Seller> findByStoreIdIn(@Param("storeIds") List<Long> storeIds);

    @Modifying
    @Query("UPDATE Seller s SET s.store = null WHERE s.store.id IN :storeIds")
    void clearStoreByStoreIdIn(@Param("storeIds") List<Long> storeIds);
}
