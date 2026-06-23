package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.order.domain.Order;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderDSLRepository {

    @Query("SELECT o FROM Order o JOIN FETCH o.member WHERE o.id = :id")
    Optional<Order> findByIdWithMember(@Param("id") Long id);
}
