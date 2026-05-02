package com.bbangle.bbangle.charge.domain;

import com.bbangle.bbangle.charge.domain.enums.ChargeCategory;
import com.bbangle.bbangle.charge.domain.enums.ChargeReferenceType;
import com.bbangle.bbangle.charge.domain.enums.ChargeTransactionStatus;
import com.bbangle.bbangle.common.domain.CreatedAtBaseEntity;
import com.bbangle.bbangle.seller.domain.Seller;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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
import org.hibernate.annotations.Comment;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "charge_history")
@Entity
public class ChargeHistory extends CreatedAtBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("판매자 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Seller seller;

    @Comment("거래 구분 (ACCUMULATE=적립/DEDUCT=소진)")
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)", nullable = false)
    private ChargeCategory category;

    @Comment("거래 금액")
    @Column(columnDefinition = "DECIMAL(15,2)", nullable = false)
    private BigDecimal amount;

    @Comment("거래 후 잔액")
    @Column(columnDefinition = "DECIMAL(15,2)", nullable = false)
    private BigDecimal balanceAfter;

    @Comment("거래 상태 (PENDING/COMPLETED)")
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(20)", nullable = false)
    private ChargeTransactionStatus status;

    @Comment("거래 발생 일자")
    @Column(nullable = false)
    private LocalDate baseDate;

    @Comment("참조 타입 (SETTLEMENT/WITHDRAWAL)")
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(30)", nullable = false)
    private ChargeReferenceType referenceType;

    @Comment("참조 ID (정산ID 또는 출금신청ID)")
    @Column(nullable = false)
    private Long referenceId;

    @Builder
    private ChargeHistory(
        Seller seller,
        ChargeCategory category,
        BigDecimal amount,
        BigDecimal balanceAfter,
        ChargeTransactionStatus status,
        LocalDate baseDate,
        ChargeReferenceType referenceType,
        Long referenceId) {
        this.seller = seller;
        this.category = category;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.status = status;
        this.baseDate = baseDate;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    /**
     * 거래내역 생성 - 정산
     */
    public static ChargeHistory fromSettlement(
        Seller seller,
        BigDecimal amount,
        BigDecimal balanceAfter,
        ChargeTransactionStatus status,
        LocalDate baseDate,
        Long settlementId) {
        return ChargeHistory.builder()
            .seller(seller)
            .category(ChargeCategory.ACCUMULATE)
            .amount(amount)
            .balanceAfter(balanceAfter)
            .status(status)
            .baseDate(baseDate)
            .referenceType(ChargeReferenceType.SETTLEMENT)
            .referenceId(settlementId)
            .build();
    }

    /**
     * 거래내역 생성 - 출금
     */
    public static ChargeHistory fromWithdrawal(
        Seller seller,
        BigDecimal amount,
        BigDecimal balanceAfter,
        ChargeTransactionStatus status,
        LocalDate baseDate,
        Long withdrawalRequestId) {
        return ChargeHistory.builder()
            .seller(seller)
            .category(ChargeCategory.DEDUCT)
            .amount(amount)
            .balanceAfter(balanceAfter)
            .status(status)
            .baseDate(baseDate)
            .referenceType(ChargeReferenceType.WITHDRAWAL)
            .referenceId(withdrawalRequestId)
            .build();
    }
}
