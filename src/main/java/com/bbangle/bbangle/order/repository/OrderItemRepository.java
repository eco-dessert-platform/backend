package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.order.domain.OrderItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderIdAndIdIn(Long orderId, List<Long> ids);

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.order WHERE oi.id = :id")
    Optional<OrderItem> findByIdWithOrder(@Param("id") Long id);

    @Query("SELECT oi FROM OrderItem oi JOIN FETCH oi.order WHERE oi.order.id = :orderId AND oi.id IN :ids")
    List<OrderItem> findByOrderIdAndIdInWithOrder(@Param("orderId") Long orderId, @Param("ids") List<Long> ids);

    @Query(value = """
        SELECT COUNT(*)
        FROM order_item oi
        JOIN product p ON p.id = oi.product_id
        WHERE oi.order_id = :orderId
          AND oi.id IN (:orderItemIds)
          AND p.store_id = :storeId
        """, nativeQuery = true)
    long countOwnedOrderItems(
        @Param("orderId") Long orderId,
        @Param("orderItemIds") List<Long> orderItemIds,
        @Param("storeId") Long storeId
    );
}
