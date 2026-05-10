package com.bbangle.bbangle.vat.seller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "판매자 부가세 신고 내역 조회 응답")
public record SellerVatSummaryResponse(
    @Schema(description = "조회 시작 월", example = "2024-09")
    String startMonth,

    @Schema(description = "조회 종료 월", example = "2025-04")
    String endMonth,

    @Schema(description = "월별 부가세 신고 내역")
    List<SellerVatMonthlyItem> items
) {
}
