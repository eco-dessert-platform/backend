package com.bbangle.bbangle.order.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.seller.domain.Seller;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
@Table(name = "orders")
@Entity
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", columnDefinition = "VARCHAR(30)")
    private String orderNumber;

    @Column(name = "order_group_id", length = 30)
    private String orderGroupId;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "buyer_name", columnDefinition = "VARCHAR(20)")
    private String buyerName;

    @Column(name = "buyer_phone", columnDefinition = "VARCHAR(20)")
    private String buyerPhone;

    @Column(name = "buyer_sub_phone", columnDefinition = "VARCHAR(20)")
    private String buyerSubPhone;

    @Column(name = "delivery_fee")
    private Integer deliveryFee;

    @Column(name = "total_amount")
    private Integer totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
    private Payment payment;

    /**
     * 주문에 주문 항목을 추가합니다. 양방향 연관관계를 안전하게 설정합니다.
     */
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    /**
     * 여러 주문 항목을 한 번에 추가합니다.
     */
    public void addOrderItems(List<OrderItem> orderItems) {
        orderItems.forEach(this::addOrderItem);
    }

}
