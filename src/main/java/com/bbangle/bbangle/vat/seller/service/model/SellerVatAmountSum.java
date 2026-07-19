package com.bbangle.bbangle.vat.seller.service.model;

import com.bbangle.bbangle.vat.seller.controller.dto.response.SellerVatMonthlyItem;
import com.bbangle.bbangle.vat.seller.provider.dto.SellerVatSettlementRow;
import java.time.YearMonth;

public class SellerVatAmountSum {

    private long taxableSalesAmount;
    private long taxFreeSalesAmount;
    private long creditCardAmount;
    private long cashReceiptIncomeDeductionAmount;
    private long cashReceiptExpenseProofAmount;
    private long etcAmount;

    public void add(SellerVatSettlementRow row) {
        taxableSalesAmount += value(row.taxableSalesAmount());
        taxFreeSalesAmount += value(row.taxFreeSalesAmount());
        creditCardAmount += value(row.creditCardAmount());
        cashReceiptIncomeDeductionAmount += value(row.cashReceiptIncomeDeductionAmount());
        cashReceiptExpenseProofAmount += value(row.cashReceiptExpenseProofAmount());
        etcAmount += value(row.etcAmount());
    }

    public SellerVatMonthlyItem toMonthlyItem(YearMonth month) {
        return new SellerVatMonthlyItem(
            month.toString(),
            taxableSalesAmount,
            taxFreeSalesAmount,
            creditCardAmount,
            cashReceiptIncomeDeductionAmount,
            cashReceiptExpenseProofAmount,
            etcAmount
        );
    }

    private long value(Long amount) {
        return amount == null ? 0L : amount;
    }
}
