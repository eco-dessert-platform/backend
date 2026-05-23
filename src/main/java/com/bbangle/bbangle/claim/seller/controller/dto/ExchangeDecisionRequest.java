package com.bbangle.bbangle.claim.seller.controller.dto;

import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "교환 요청 승인/거절 처리 DTO")
public record ExchangeDecisionRequest(

    @Schema(description = "처리 유형", example = "APPROVE", allowableValues = {"APPROVE", "REJECT"})
    @NotNull
    DecisionType decisionType,

    @Schema(description = "처리 사유", example = "교환 승인", maxLength = 500)
    String reason

) {
}
