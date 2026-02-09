package com.bbangle.bbangle.claim.seller.controller.swagger;

import com.bbangle.bbangle.claim.seller.controller.dto.CancelDecisionRequest;
import com.bbangle.bbangle.common.dto.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cancel", description = "(셀러) 취소 관리 API")
public interface SellerCancelApi {

    @Operation(
        summary = "취소 요청 승인/거절",
        description = "취소 요청에 대해 승인 또는 거절 처리를 수행한다."
    )
    CommonResult cancelDecision(
        @Parameter(description = "취소 요청 ID", example = "101", required = true)
        Long cancelId,
        CancelDecisionRequest cancelDecisionRequest,
        @Parameter(hidden = true) Long sellerId
    );
}
