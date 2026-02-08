package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.order.domain.OrderDelivery;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {

    @Query("""
        SELECT od FROM OrderDelivery od
        WHERE od.orderItem.id IN :orderItemIds
        AND od.createdAt = (
            SELECT MAX(od2.createdAt)
            FROM OrderDelivery od2
            WHERE od2.orderItem.id = od.orderItem.id
        )
        """)
    List<OrderDelivery> findLatestByOrderItemIds(@Param("orderItemIds") List<Long> orderItemIds);
}
