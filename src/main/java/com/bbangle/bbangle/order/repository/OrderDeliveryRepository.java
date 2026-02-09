package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.order.domain.OrderDelivery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDeliveryRepository extends JpaRepository<OrderDelivery, Long> {

    @Query("SELECT od FROM OrderDelivery od WHERE od.orderItem.id = :orderItemId")
    Optional<OrderDelivery> findByOrderItemId(@Param("orderItemId") Long orderItemId);

    @Query("SELECT od FROM OrderDelivery od JOIN FETCH od.orderItem WHERE od.orderItem.id IN :orderItemIds")
    List<OrderDelivery> findByOrderItemIdIn(@Param("orderItemIds") List<Long> orderItemIds);

}
