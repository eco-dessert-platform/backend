package com.bbangle.bbangle.payment.repository;

import com.bbangle.bbangle.payment.domain.Payment;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderOrderNumber(String orderNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT p FROM Payment p JOIN FETCH p.order o LEFT JOIN FETCH o.orderItems WHERE o.orderGroupId = :orderGroupId")
    List<Payment> findByOrderGroupIdWithLock(@Param("orderGroupId") String orderGroupId);
}
