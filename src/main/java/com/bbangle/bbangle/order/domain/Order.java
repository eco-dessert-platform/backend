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

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "buyer_name", columnDefinition = "VARCHAR(20)")
    private String buyerName;

    @Column(name = "buyer_phone", columnDefinition = "VARCHAR(20)")
    private String buyerPhone;

    @Column(name = "buyer_sub_phone", columnDefinition = "VARCHAR(20)")
    private String buyerSubPhone;

    @Column(name = "buyer_email", columnDefinition = "VARCHAR(100)")
    private String buyerEmail;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    /**
     * 스토어(판매자) 단위 주문을 생성한다.
     *
     * <p>{@code @Builder} 는 필드 초기화식({@code = new ArrayList<>()})을 무시하므로
     * {@code orderItems} 를 반드시 빈 리스트로 채워야 {@link #addOrderItem} 이 동작한다.
     */
    public static Order create(
        String orderNumber,
        Member member,
        Seller seller,
        Payment payment,
        String buyerName,
        String buyerPhone,
        String buyerEmail,
        Integer deliveryFee,
        Integer totalAmount
    ) {
        return Order.builder()
            .orderNumber(orderNumber)
            .orderDate(LocalDateTime.now())
            .member(member)
            .seller(seller)
            .payment(payment)
            .buyerName(buyerName)
            .buyerPhone(buyerPhone)
            .buyerEmail(buyerEmail)
            .deliveryFee(deliveryFee)
            .totalAmount(totalAmount)
            .orderItems(new ArrayList<>())
            .build();
    }

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
