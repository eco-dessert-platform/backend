package com.bbangle.bbangle.paymenthold.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldStatus;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.settlement.domain.SettlementItem;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payment_hold")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentHold extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_item_id", nullable = false)
    private SettlementItem settlementItem;

    @Column(name = "settlement_number", nullable = false, columnDefinition = "VARCHAR(50)")
    private String settlementNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(20)")
    private PaymentHoldStatus status;

    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "settlement_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal settlementAmount;

    @Builder
    private PaymentHold(
        Seller seller,
        SettlementItem settlementItem,
        String settlementNumber,
        PaymentHoldStatus status,
        LocalDate baseDate,
        LocalDate completedDate,
        BigDecimal settlementAmount
    ) {
        this.seller = seller;
        this.settlementItem = settlementItem;
        this.settlementNumber = settlementNumber;
        this.status = status;
        this.baseDate = baseDate;
        this.completedDate = completedDate;
        this.settlementAmount = settlementAmount;
    }
}
