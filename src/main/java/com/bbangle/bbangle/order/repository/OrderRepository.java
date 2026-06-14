package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.order.domain.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderDSLRepository {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByOrderGroupId(String orderGroupId);
}
