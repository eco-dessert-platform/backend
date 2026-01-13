package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
