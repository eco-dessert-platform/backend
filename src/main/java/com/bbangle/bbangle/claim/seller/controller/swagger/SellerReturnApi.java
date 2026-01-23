package com.bbangle.bbangle.claim.seller.controller.swagger;

import com.bbangle.bbangle.claim.seller.controller.dto.ReturnDecisionRequest;
import com.bbangle.bbangle.common.dto.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Return", description = "(셀러) 반품 관리 API")
public interface SellerReturnApi {

    @Operation(
        summary = "반품 요청 승인/거절",
        description = "반품 요청에 대해 승인 또는 거절 처리를 수행한다."
    )
    CommonResult returnDecision(
        @Parameter(description = "반품 요청 ID", example = "101", required = true)
        Long returnId,
        ReturnDecisionRequest returnDecisionRequest,
        @Parameter(hidden = true) Long sellerId
    );
}