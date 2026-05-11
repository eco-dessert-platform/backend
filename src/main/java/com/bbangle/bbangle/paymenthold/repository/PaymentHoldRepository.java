package com.bbangle.bbangle.paymenthold.repository;

import com.bbangle.bbangle.paymenthold.domain.PaymentHold;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentHoldRepository extends JpaRepository<PaymentHold, Long>, PaymentHoldDSLRepository {

}
