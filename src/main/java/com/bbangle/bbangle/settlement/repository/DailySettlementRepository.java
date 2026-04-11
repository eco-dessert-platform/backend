package com.bbangle.bbangle.settlement.repository;

import com.bbangle.bbangle.settlement.domain.DailySettlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailySettlementRepository
    extends JpaRepository<DailySettlement, Long>, DailySettlementDSLRepository {

}
