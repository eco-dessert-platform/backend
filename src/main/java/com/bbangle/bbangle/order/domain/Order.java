package com.bbangle.bbangle.order.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "orders")
@Entity
public class Order extends BaseEntity{

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

    @Column(name = "delivery_fee")
    private int deliveryFee;

    @Column(name = "total_amount")
    private int totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;



}
