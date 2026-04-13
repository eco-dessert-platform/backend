package com.bbangle.bbangle.claim.seller.controller.swagger;

import com.bbangle.bbangle.claim.seller.controller.dto.RegisterReturnInvoiceRequest;
import com.bbangle.bbangle.claim.seller.controller.dto.ReturnDecisionRequest;
import com.bbangle.bbangle.claim.seller.controller.dto.UpdateReturnInvoiceRequest;
import com.bbangle.bbangle.claim.seller.controller.dto.UpdateReturnInvoiceResponse;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
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
        ReturnDecisionRequest returnDecisionRequest,
        @Parameter(hidden = true) Long sellerId
    );

    @Operation(
        summary = "반품 최종 처리 (환불 확정)",
        description = "검수 완료(INSPECTION_APPROVED) 상태의 반품을 최종 승인하여 COMPLETED로 변경하고 환불을 확정한다."
    )
    CommonResult processReturn(
        @Parameter(description = "반품 요청 ID") Long returnId,
        @Parameter(hidden = true) Long sellerId
    );

    @Operation(
        summary = "반품 운송장 입력",
        description = "반품 승인(APPROVED) 상태의 클레임에 택배사 코드와 운송장 번호를 등록하고, 상태를 반품 수거 예정(PICKUP_SCHEDULED)으로 변경한다."
    )
    CommonResult registerReturnInvoice(
        @Parameter(description = "반품 요청 ID") Long returnId,
        RegisterReturnInvoiceRequest request,
        @Parameter(hidden = true) Long sellerId
    );

    @Operation(
        summary = "반품 수거 운송장 수정",
        description = "수거 예정(PICKUP_SCHEDULED) 상태일 때만 택배사 코드와 운송장 번호를 수정할 수 있다. "
            + "수거가 시작(PICKED_UP)된 이후에는 물류 흐름이 시작된 것으로 간주하여 수정이 차단된다."
    )
    SingleResult<UpdateReturnInvoiceResponse> updateReturnInvoice(
        @Parameter(description = "반품 요청 ID") Long returnId,
        UpdateReturnInvoiceRequest request,
        @Parameter(hidden = true) Long sellerId
    );
}