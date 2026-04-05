package com.bbangle.bbangle.charge.seller.controller.dto.response;

import com.bbangle.bbangle.charge.domain.enums.ChargeCategory;
import com.bbangle.bbangle.charge.domain.enums.ChargeTransactionStatus;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "충전금 현황 조회 응답 DTO")
public record ChargeBalanceResponse(
    @Schema(description = "현재 충전금 잔액", example = "2000000")
    BigDecimal chargeBalance,

    @Schema(description = "충전금 거래내역 페이지네이션")
    BbanglePageResponse<ChargeTransactionResponse> pageResponse
) {

  @Schema(description = "충전금 거래내역")
  public record ChargeTransactionResponse(
      @Schema(description = "거래 발생 일자", example = "2025-09-01", pattern = "yyyy-MM-dd")
      LocalDate baseDate,

      @Schema(description = "정산 ID", example = "250401A1F7")
      String settlementId,

      @Schema(description = "거래 구분", example = "ACCUMULATE", allowableValues = {"ACCUMULATE", "DEDUCT"})
      ChargeCategory category,

      @Schema(description = "거래 금액", example = "123456")
      long amount,

      @Schema(description = "거래 상태", example = "PENDING", allowableValues = {"PENDING", "COMPLETED"})
      ChargeTransactionStatus status
  ) {}
}
