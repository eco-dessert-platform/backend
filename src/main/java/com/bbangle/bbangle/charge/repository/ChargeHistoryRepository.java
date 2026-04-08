package com.bbangle.bbangle.charge.repository;

import com.bbangle.bbangle.charge.domain.ChargeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeHistoryRepository extends JpaRepository<ChargeHistory, Long> {

}
