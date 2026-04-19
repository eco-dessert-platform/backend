package com.bbangle.bbangle.charge.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.seller.domain.Seller;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "charge_withdrawal_request")
@Entity
public class ChargeWithdrawalRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("판매자 ID")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Seller seller;

    @Comment("출금 신청 금액")
    @Column(columnDefinition = "DECIMAL(15,2)", nullable = false)
    private BigDecimal withdrawalAmount;

    @Comment("은행명")
    @Column(columnDefinition = "VARCHAR(30)", nullable = false)
    private String bankName;

    @Comment("예금주명")
    @Column(columnDefinition = "VARCHAR(30)", nullable = false)
    private String accountHolder;

    @Comment("계좌번호")
    @Column(columnDefinition = "VARCHAR(20)", nullable = false)
    private String accountNumber;

    @Comment("출금 처리 성공 여부")
    @Column(columnDefinition = "tinyint", nullable = false)
    private Boolean success;

    @Comment("출금 실패 이유")
    @Column(columnDefinition = "VARCHAR(255)")
    private String failureReason;

    @Builder
    private ChargeWithdrawalRequest(
        Seller seller,
        BigDecimal withdrawalAmount,
        String bankName,
        String accountHolder,
        String accountNumber,
        Boolean success,
        String failureReason) {
        this.seller = seller;
        this.withdrawalAmount = withdrawalAmount;
        this.bankName = bankName;
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
        this.success = success;
        this.failureReason = failureReason;
    }

    /**
     * 출금 신청 성공 생성
     */
    public static ChargeWithdrawalRequest createSuccess(
        Seller seller,
        BigDecimal withdrawalAmount,
        String bankName,
        String accountHolder,
        String accountNumber) {
        return ChargeWithdrawalRequest.builder()
            .seller(seller)
            .withdrawalAmount(withdrawalAmount)
            .bankName(bankName)
            .accountHolder(accountHolder)
            .accountNumber(accountNumber)
            .success(true)
            .build();
    }

    /**
     * 출금 신청 실패 생성
     */
    public static ChargeWithdrawalRequest createFailure(
        Seller seller,
        BigDecimal withdrawalAmount,
        String bankName,
        String accountHolder,
        String accountNumber,
        String failureReason) {
        return ChargeWithdrawalRequest.builder()
            .seller(seller)
            .withdrawalAmount(withdrawalAmount)
            .bankName(bankName)
            .accountHolder(accountHolder)
            .accountNumber(accountNumber)
            .success(false)
            .failureReason(failureReason)
            .build();
    }
}
