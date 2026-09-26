package com.bbangle.bbangle.payment.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.member.domain.Member;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 단위 애그리거트.
 *
 * <p>결제 1건이 스토어별 주문({@link com.bbangle.bbangle.order.domain.Order}) N건을 묶는다.
 * 연관관계 주인은 {@code Order} 이며({@code orders.payment_id}), Payment 는 역방향 참조를 두지 않는다.
 * 결제 단위로 주문을 찾아야 하면 {@code OrderRepository} 를 통해 조회한다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "payment")
@Entity
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** PG 에 전달하는 주문번호. 결제 단위 식별자다. */
    @Column(name = "payment_number", length = 64, nullable = false)
    private String paymentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /** 결제 총액. 묶인 주문들의 totalAmount 합계다. */
    @Column(name = "total_amount")
    private Integer totalAmount;

    @Column(name = "payment_status", length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_method", length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    /** PG 거래 키. 승인 전에는 null 이다. */
    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    private LocalDateTime paidAt;

    @Column(name = "approval_number", length = 20)
    private String approvalNumber;

    @Column(name = "card_type", length = 20)
    @Enumerated(EnumType.STRING)
    private CardType cardType;

    @Column(name = "card_number", length = 255)
    private String cardNumber;

    @Column(name = "installment", length = 20)
    private String installment;

    @Builder(access = AccessLevel.PRIVATE)
    private Payment(String paymentNumber,
                    Member member,
                    Integer totalAmount,
                    PaymentStatus paymentStatus,
                    PaymentMethod paymentMethod,
                    String paymentKey,
                    LocalDateTime paidAt,
                    String approvalNumber,
                    CardType cardType,
                    String cardNumber,
                    String installment) {
        this.paymentNumber = paymentNumber;
        this.member = member;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paymentKey = paymentKey;
        this.paidAt = paidAt;
        this.approvalNumber = approvalNumber;
        this.cardType = cardType;
        this.cardNumber = cardNumber;
        this.installment = installment;
    }

    /**
     * 주문 생성 시점의 결제. 아직 PG 승인 전이므로 PENDING 으로 시작한다.
     */
    public static Payment pending(
        String paymentNumber,
        Member member,
        Integer totalAmount,
        PaymentMethod paymentMethod
    ) {
        return Payment.builder()
            .paymentNumber(paymentNumber)
            .member(member)
            .totalAmount(totalAmount)
            .paymentStatus(PaymentStatus.PENDING)
            .paymentMethod(paymentMethod)
            .build();
    }

    public static Payment create(
        String paymentNumber,
        Member member,
        Integer totalAmount,
        PaymentStatus paymentStatus,
        PaymentMethod paymentMethod,
        LocalDateTime paidAt
    ) {
        return Payment.builder()
            .paymentNumber(paymentNumber)
            .member(member)
            .totalAmount(totalAmount)
            .paymentStatus(paymentStatus)
            .paymentMethod(paymentMethod)
            .paidAt(paidAt)
            .build();
    }
}
