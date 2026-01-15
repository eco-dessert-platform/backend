package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.order.domain.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderIdAndIdIn(Long orderId, List<Long> ids);
}
