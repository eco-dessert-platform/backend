package com.bbangle.bbangle.vat.seller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "판매자 부가세 신고 월별 내역")
public record SellerVatMonthlyItem(
    @Schema(description = "정산 월", example = "2025-04")
    String month,

    @Schema(description = "과세 매출금액", example = "1875000")
    Long taxableSalesAmount,

    @Schema(description = "면세 매출금액", example = "320000")
    Long taxFreeSalesAmount,

    @Schema(description = "신용카드 금액", example = "1250000")
    Long creditCardAmount,

    @Schema(description = "현금영수증 소득공제 금액", example = "430000")
    Long cashReceiptIncomeDeductionAmount,

    @Schema(description = "현금영수증 지출증빙 금액", example = "260000")
    Long cashReceiptExpenseProofAmount,

    @Schema(description = "기타 금액", example = "255000")
    Long etcAmount
) {
}
