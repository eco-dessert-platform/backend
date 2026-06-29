package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.order.domain.OrderItem;
import java.time.LocalDateTime;
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

    @Query(value = """
        SELECT COUNT(*)
        FROM order_item oi
        JOIN product p ON p.id = oi.product_id
        WHERE oi.id IN (:orderItemIds)
          AND p.store_id = :storeId
        """, nativeQuery = true)
    long countOwnedOrderItemsByStoreId(
        @Param("orderItemIds") List<Long> orderItemIds,
        @Param("storeId") Long storeId
    );

    @Query("""
        SELECT oi FROM OrderItem oi
        JOIN FETCH oi.order
        JOIN FETCH oi.product p
        JOIN FETCH p.board
        WHERE oi.id IN :ids
        """)
    List<OrderItem> findWithOrderAndProductByIdIn(@Param("ids") List<Long> ids);

    /**
     * 회원 소유의 단일 주문상품을 조회합니다. (수동 구매확정 시 소유권 검증용)
     */
    @Query("""
        SELECT oi FROM OrderItem oi
        JOIN FETCH oi.order o
        WHERE oi.id = :orderItemId
          AND o.id = :orderId
          AND o.member.id = :memberId
        """)
    Optional<OrderItem> findOwnedOrderItem(
        @Param("memberId") Long memberId,
        @Param("orderId") Long orderId,
        @Param("orderItemId") Long orderItemId
    );

    /**
     * 자동 구매확정 대상 조회.
     * 상품발송(SHIPPED) 상태이면서, 배송완료(DELIVERED) 처리된 지 기준시각(cutoff) 이상 지난 주문상품을 찾습니다.
     */
    @Query("""
        SELECT oi FROM OrderItem oi
        WHERE oi.orderStatus = com.bbangle.bbangle.order.domain.model.OrderStatus.SHIPPED
          AND EXISTS (
            SELECT 1 FROM OrderDelivery od
            WHERE od.orderItem = oi
              AND od.status = com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus.DELIVERED
              AND od.shipping.deliveredAt IS NOT NULL
              AND od.shipping.deliveredAt <= :cutoff
          )
        """)
    List<OrderItem> findAutoConfirmTargets(@Param("cutoff") LocalDateTime cutoff);
}
