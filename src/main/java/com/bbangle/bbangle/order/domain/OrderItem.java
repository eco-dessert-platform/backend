package com.bbangle.bbangle.order.domain;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Getter
@Table(name = "order_item")
@Entity
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "product_price")
    private Integer productPrice;

    @Column(name = "unit_price")
    private Integer unitPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", columnDefinition = "VARCHAR(20)")
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", columnDefinition = "VARCHAR(20)")
    private OrderDeliveryStatus orderDeliveryStatus;

    @Column(name = "total_price")
    private Integer totalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "orderItem", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<OrderDelivery> orderDeliveries = new ArrayList<>();

    public boolean confirmOrder() {
        if (this.orderStatus != OrderStatus.PAYMENT_COMPLETED) {
            return false;
        }
        this.orderStatus = OrderStatus.ORDER_CONFIRMED;
        return true;
    }

    /**
     * 양방향 연관관계 설정용 패키지 전용 메서드
     * Order.addOrderItem()을 통해서만 호출되어야 합니다.
     */
    void setOrder(Order order) {
        this.order = order;
    }

    /**
     * 주문 항목에 배송 정보를 추가합니다.
     * 양방향 연관관계를 안전하게 설정합니다.
     */
    public void addOrderDelivery(OrderDelivery orderDelivery) {
        this.orderDeliveries.add(orderDelivery);
        orderDelivery.setOrderItem(this);
    }

    /**
     * 여러 배송 정보를 한 번에 추가합니다.
     */
    public void addOrderDeliveries(List<OrderDelivery> orderDeliveries) {
        orderDeliveries.forEach(this::addOrderDelivery);
    }

}
