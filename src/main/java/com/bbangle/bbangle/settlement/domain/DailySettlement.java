package com.bbangle.bbangle.settlement.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.settlement.domain.model.SettlementMethod;
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
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "daily_settlement")
public class DailySettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Builder
    private DailySettlement(
        LocalDate scheduledDate,
        LocalDate completedDate,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal deductibleRefund,
        BigDecimal withHoldingPayment,
        SettlementMethod settlementMethod,
        Seller seller,
        String settlementNumber,
        BigDecimal deliveryFeeChange,
        BigDecimal balanceOffset
    ) {
        this.scheduledDate = scheduledDate;
        this.completedDate = completedDate;
        this.amount = amount;
        this.fee = fee;
        this.deductibleRefund = deductibleRefund;
        this.withHoldingPayment = withHoldingPayment;
        this.settlementMethod = settlementMethod;
        this.seller = seller;
        this.settlementNumber = settlementNumber;
        this.deliveryFeeChange = deliveryFeeChange;
        this.balanceOffset = balanceOffset;
    }

    // 정산예정일
    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    // 정산완료일
    @Column(name = "completed_date")
    private LocalDate completedDate;

    // 결제금액
    @Column(name= "amount" , precision = 15, scale = 2)
    private BigDecimal amount;

    // 수수료
    @Column(name= "fee" , precision = 15, scale = 2)
    private BigDecimal fee; // 수수료

    // 공제/환급
    @Column(name= "deductible_refund" , precision = 15, scale = 2)
    private BigDecimal deductibleRefund; // 공제/환급

    // 지급보류
    @Column(name= "with_holding_payment" , precision = 15, scale = 2)
    private BigDecimal withHoldingPayment;

    // 정산방식
    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_method", columnDefinition = "VARCHAR(20)")
    private SettlementMethod settlementMethod;

    // 판매자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    // 정산번호 (UI 표시용 정산ID)
    @Column(name = "settlement_number", columnDefinition = "VARCHAR(50)")
    private String settlementNumber;

    // 공제/환급 상세 - 배송비 금액 변동
    @Column(name = "delivery_fee_change", precision = 15, scale = 2)
    private BigDecimal deliveryFeeChange;

    // 공제/환급 상세 - 충전금 상계
    @Column(name = "balance_offset", precision = 15, scale = 2)
    private BigDecimal balanceOffset;

    // 정산금액 = 결제금액(a) + 수수료(b) + 공제/환급(c) + 지급보류(d)
    public BigDecimal getTotalSettlementAmount() {
        return Stream.of(amount, fee, deductibleRefund, withHoldingPayment)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
