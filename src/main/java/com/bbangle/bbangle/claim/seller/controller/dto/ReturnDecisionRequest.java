package com.bbangle.bbangle.claim.seller.controller.dto;

import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "반품 요청 승인/거절 요청 DTO")
public record ReturnDecisionRequest(

    @Schema(description = "반품 요청 ID 리스트", example = "[20, 21, 22]")
    @NotEmpty
    List<Long> returnIds,

    @Schema(description = "처리 유형", example = "APPROVE", allowableValues = {"APPROVE", "REJECT"})
    @NotNull
    DecisionType decisionType,

    @Schema(description = "처리 사유", example = "검수 완료", maxLength = 500)
    String reason

) {
}