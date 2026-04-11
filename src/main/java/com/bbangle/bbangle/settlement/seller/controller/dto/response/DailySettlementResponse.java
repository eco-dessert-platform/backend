package com.bbangle.bbangle.settlement.seller.controller.dto.response;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.settlement.domain.DailySettlement;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

public class DailySettlementResponse {

    @Schema(description = "일별 정산내역 조회 응답 (페이지 + 요약)")
    public record DailySettlementPageResponse(
        @Schema(description = "정산 목록 페이지")
        BbanglePageResponse<DailySettlementSummary> settlements,

        @Schema(description = "정산 요약 정보")
        SettlementSummaryInfo summary
    ) {

    }

    @Schema(description = "정산 요약 정보")
    public record SettlementSummaryInfo(
        @Schema(description = "정산예정일 최소", example = "2025-03-02")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate scheduledDateMin,

        @Schema(description = "정산예정일 최대", example = "2025-03-05")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate scheduledDateMax,

        @Schema(description = "총 정산 금액", example = "2000000")
        BigDecimal totalSettlementAmount
    ) {

    }

    @Schema(description = "일별 정산 내역")
    public record DailySettlementSummary(
        @Schema(description = "정산ID", example = "250401A1F7")
        String settlementNumber,

        @Schema(description = "정산예정일")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate scheduledDate,

        @Schema(description = "정산완료일")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate completedDate,

        @Schema(description = "정산금액(a+b+c+d)")
        BigDecimal totalSettlementAmount,

        @Schema(description = "결제금액(a)")
        BigDecimal amount,

        @Schema(description = "수수료(b)")
        BigDecimal fee,

        @Schema(description = "공제/환급(c)")
        BigDecimal deductibleRefund,

        @Schema(description = "공제/환급 상세")
        DeductibleRefundDetail deductibleRefundDetail,

        @Schema(description = "지급보류(d)")
        BigDecimal withHoldingPayment,

        @Schema(description = "정산방식", example = "계좌이체")
        String settlementMethod
    ) {

        public static DailySettlementSummary from(DailySettlement entity) {
            return new DailySettlementSummary(
                entity.getSettlementNumber(),
                entity.getScheduledDate(),
                entity.getCompletedDate(),
                entity.getTotalSettlementAmount(),
                entity.getAmount(),
                entity.getFee(),
                entity.getDeductibleRefund(),
                new DeductibleRefundDetail(
                    entity.getDeliveryFeeChange(),
                    entity.getBalanceOffset()
                ),
                entity.getWithHoldingPayment(),
                entity.getSettlementMethod() != null
                    ? entity.getSettlementMethod().getDescription()
                    : null
            );
        }
    }

    @Schema(description = "공제/환급 상세 내역")
    public record DeductibleRefundDetail(
        @Schema(description = "배송비 금액 변동")
        BigDecimal deliveryFeeChange,

        @Schema(description = "충전금 상계")
        BigDecimal balanceOffset
    ) {

    }

}
