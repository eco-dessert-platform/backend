package com.bbangle.bbangle.order.domain;

import com.bbangle.bbangle.common.domain.CreatedAtBaseEntity;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "order_item_history")
@Entity
public class OrderItemHistory extends CreatedAtBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", columnDefinition = "VARCHAR(20)")
    private OrderStatus Orderstatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Builder
    private OrderItemHistory(OrderStatus orderstatus, OrderItem orderItem) {
        this.Orderstatus = orderstatus;
        this.orderItem = orderItem;
    }

    public static OrderItemHistory create(OrderItem orderItem) {
        return OrderItemHistory.builder()
            .orderstatus(orderItem.getOrderStatus())
            .orderItem(orderItem)
            .build();
    }

}
