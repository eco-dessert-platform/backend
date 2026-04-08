package com.bbangle.bbangle.charge.repository;

import com.bbangle.bbangle.charge.domain.ChargeWithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeWithdrawalRequestRepository extends JpaRepository<ChargeWithdrawalRequest, Long> {

}
