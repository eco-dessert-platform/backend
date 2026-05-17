package com.bbangle.bbangle.claim.seller.controller.swagger;

import com.bbangle.bbangle.claim.seller.controller.dto.ExchangeDecisionRequest;
import com.bbangle.bbangle.common.dto.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Exchange", description = "(셀러) 교환 관리 API")
public interface SellerExchangeApi {

    @Operation(
        summary = "교환 요청 승인/거절",
        description = "REQUESTED 상태의 교환 요청에 대해 승인(APPROVE) 또는 거절(REJECT) 처리를 수행한다. "
            + "거절 시 reason은 ExchangeRequest의 sellerComment 필드에 저장된다."
    )
    CommonResult processExchange(
        @Parameter(description = "교환 요청 ID") Long exchangeId,
        ExchangeDecisionRequest request,
        @Parameter(hidden = true) Long sellerId
    );
}
