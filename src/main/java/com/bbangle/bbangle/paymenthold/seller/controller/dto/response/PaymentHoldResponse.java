package com.bbangle.bbangle.paymenthold.seller.controller.dto.response;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.paymenthold.domain.PaymentHold;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentHoldResponse {

    @Schema(description = "지급보류 조회 응답")
    public record PaymentHoldPageResponse(
        @Schema(description = "지급보류 목록 페이지")
        BbanglePageResponse<PaymentHoldSummary> paymentHolds
    ) {
    }

    @Schema(description = "지급보류 요약")
    public record PaymentHoldSummary(
        @Schema(description = "지급보류 ID", example = "1")
        Long paymentHoldId,

        @Schema(description = "정산 ID", example = "250401A1F7")
        String settlementId,

        @Schema(description = "정산 상태", example = "지급보류")
        String status,

        @Schema(description = "정산 기준일")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate settlementBaseDate,

        @Schema(description = "정산 완료일")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate settlementCompletedDate,

        @Schema(description = "정산 금액", example = "25000")
        BigDecimal settlementAmount
    ) {
        public static PaymentHoldSummary from(PaymentHold entity) {
            return new PaymentHoldSummary(
                entity.getId(),
                entity.getSettlementNumber(),
                entity.getStatus().getDescription(),
                entity.getBaseDate(),
                entity.getCompletedDate(),
                entity.getSettlementAmount()
            );
        }
    }
}
