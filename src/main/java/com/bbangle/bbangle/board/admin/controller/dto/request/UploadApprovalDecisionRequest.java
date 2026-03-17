package com.bbangle.bbangle.board.admin.controller.dto.request;

import com.bbangle.bbangle.board.domain.RejectionCategory;
import com.bbangle.bbangle.claim.domain.constant.DecisionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "업로드 상품 승인/거절 요청 DTO")
public record UploadApprovalDecisionRequest(
    @Schema(description = "처리 유형")
    @NotNull
    DecisionType decisionType,

    @Schema(
        description = "거절 사유 카테고리 (REJECT시 필수)",
        example = "INAPPROPRIATE_BRAND_NAME"
    )
    RejectionCategory rejectCategory,

    @Schema(
        description = "거절 상세 사유 (최대 500자)",
        example = "브랜드명을 무단으로 사용하여 고객에게 혼동을 줄 수 있습니다."
    )
    String rejectReason
) {}
