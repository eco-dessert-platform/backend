package com.bbangle.bbangle.payment.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.order.domain.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "payment")
@Entity
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @Column(name = "payment_status", length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_method", length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private LocalDateTime paidAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(Order order,
                    PaymentStatus paymentStatus,
                    PaymentMethod paymentMethod,
                    LocalDateTime paidAt) {
        this.order = order;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
    }

    public static Payment create(
        Order order,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        LocalDateTime paidAt) {
        return Payment.builder()
            .order(order)
            .paymentStatus(paymentStatus)
            .paymentMethod(paymentMethod)
            .paidAt(paidAt)
            .build();

    }
}
