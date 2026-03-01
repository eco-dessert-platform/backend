package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.order.domain.OrderDelivery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {

    @Query(value = """
        SELECT od.* FROM order_delivery od
        INNER JOIN (
            SELECT order_item_id, MAX(created_at) AS max_created_at
            FROM order_delivery
            WHERE order_item_id IN (:orderItemIds)
            GROUP BY order_item_id
        ) latest ON od.order_item_id = latest.order_item_id
               AND od.created_at = latest.max_created_at
        """, nativeQuery = true)
    List<OrderDelivery> findLatestByOrderItemIds(@Param("orderItemIds") List<Long> orderItemIds);

    @Query("SELECT od FROM OrderDelivery od WHERE od.orderItem.id = :orderItemId")
    Optional<OrderDelivery> findByOrderItemId(@Param("orderItemId") Long orderItemId);

    @Query("SELECT od FROM OrderDelivery od JOIN FETCH od.orderItem WHERE od.orderItem.id IN :orderItemIds")
    List<OrderDelivery> findByOrderItemIdIn(@Param("orderItemIds") List<Long> orderItemIds);


}
