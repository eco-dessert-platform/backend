package com.bbangle.bbangle.claim.seller.controller.dto;

import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "취소 요청 승인/거절 요청 DTO")
public record CancelDecisionRequest(

    @Schema(description = "주문 취소 요청 ID 리스트", example = "[10, 11, 12]")
    @NotEmpty
    List<Long> cancelIds,

    @Schema(description = "처리 유형", example = "APPROVE", allowableValues = {"APPROVE", "REJECT"})
    @NotNull
    DecisionType decisionType,

    @Schema(description = "처리 사유", example = "구성품 누락으로 인한 거부", maxLength = 500)
    String reason

) {
}
