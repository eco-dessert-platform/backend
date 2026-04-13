package com.bbangle.bbangle.charge.repository;

import com.bbangle.bbangle.charge.domain.ChargeHistory;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeHistoryRepository extends JpaRepository<ChargeHistory, Long> {

  Page<ChargeHistory> findBySellerIdAndBaseDateBetween(
      Long sellerId,
      LocalDate startDate,
      LocalDate endDate,
      Pageable pageable
  );
}
