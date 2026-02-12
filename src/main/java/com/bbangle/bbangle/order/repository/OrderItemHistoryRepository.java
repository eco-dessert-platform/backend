package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.order.domain.OrderItemHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemHistoryRepository extends JpaRepository<OrderItemHistory, Long> {
}
