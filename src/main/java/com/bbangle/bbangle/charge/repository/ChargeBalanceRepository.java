package com.bbangle.bbangle.charge.repository;

import com.bbangle.bbangle.charge.domain.ChargeBalance;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeBalanceRepository extends JpaRepository<ChargeBalance, Long> {

  Optional<ChargeBalance> findBySellerId(Long sellerId);
}
