package com.bbangle.bbangle.vat.seller.provider.dto;

import java.time.LocalDate;

public record SellerVatSettlementRow(
    Long sellerId,
    LocalDate settlementDate,
    String settlementNo,
    Long taxableSalesAmount,
    Long taxFreeSalesAmount,
    Long creditCardAmount,
    Long cashReceiptIncomeDeductionAmount,
    Long cashReceiptExpenseProofAmount,
    Long etcAmount
) {
}
