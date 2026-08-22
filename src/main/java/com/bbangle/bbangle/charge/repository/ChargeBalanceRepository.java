package com.bbangle.bbangle.charge.repository;

import com.bbangle.bbangle.charge.domain.ChargeBalance;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChargeBalanceRepository extends JpaRepository<ChargeBalance, Long> {

  Optional<ChargeBalance> findBySellerId(Long sellerId);

  boolean existsBySellerId(Long sellerId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT cb FROM ChargeBalance cb WHERE cb.seller.id = :sellerId")
  Optional<ChargeBalance> findBySellerIdWithLock(@Param("sellerId") Long sellerId);
}
