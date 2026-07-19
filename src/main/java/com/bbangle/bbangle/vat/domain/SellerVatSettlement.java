package com.bbangle.bbangle.vat.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.vat.seller.provider.dto.SellerVatSettlementRow;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "seller_vat_settlement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerVatSettlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "settlement_no", columnDefinition = "VARCHAR(50)", nullable = false)
    private String settlementNo;

    @Column(name = "taxable_sales_amount")
    private Long taxableSalesAmount;

    @Column(name = "tax_free_sales_amount")
    private Long taxFreeSalesAmount;

    @Column(name = "credit_card_amount")
    private Long creditCardAmount;

    @Column(name = "cash_receipt_income_deduction_amount")
    private Long cashReceiptIncomeDeductionAmount;

    @Column(name = "cash_receipt_expense_proof_amount")
    private Long cashReceiptExpenseProofAmount;

    @Column(name = "etc_amount")
    private Long etcAmount;

    @Builder
    private SellerVatSettlement(
        Seller seller,
        LocalDate settlementDate,
        String settlementNo,
        Long taxableSalesAmount,
        Long taxFreeSalesAmount,
        Long creditCardAmount,
        Long cashReceiptIncomeDeductionAmount,
        Long cashReceiptExpenseProofAmount,
        Long etcAmount
    ) {
        this.seller = seller;
        this.settlementDate = settlementDate;
        this.settlementNo = settlementNo;
        this.taxableSalesAmount = taxableSalesAmount;
        this.taxFreeSalesAmount = taxFreeSalesAmount;
        this.creditCardAmount = creditCardAmount;
        this.cashReceiptIncomeDeductionAmount = cashReceiptIncomeDeductionAmount;
        this.cashReceiptExpenseProofAmount = cashReceiptExpenseProofAmount;
        this.etcAmount = etcAmount;
    }

    public SellerVatSettlementRow toRow() {
        return new SellerVatSettlementRow(
            seller != null ? seller.getId() : null,
            settlementDate,
            settlementNo,
            taxableSalesAmount,
            taxFreeSalesAmount,
            creditCardAmount,
            cashReceiptIncomeDeductionAmount,
            cashReceiptExpenseProofAmount,
            etcAmount
        );
    }
}
