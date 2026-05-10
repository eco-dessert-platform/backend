package com.bbangle.bbangle.vat.repository;

import com.bbangle.bbangle.vat.domain.SellerVatSettlement;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerVatSettlementRepository extends JpaRepository<SellerVatSettlement, Long> {

    List<SellerVatSettlement> findBySellerIdAndSettlementDateBetweenOrderBySettlementDateDescSettlementNoDesc(
        Long sellerId,
        LocalDate startDate,
        LocalDate endDate
    );
}
