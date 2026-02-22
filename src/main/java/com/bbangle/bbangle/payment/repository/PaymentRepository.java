package com.bbangle.bbangle.payment.repository;

import com.bbangle.bbangle.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
