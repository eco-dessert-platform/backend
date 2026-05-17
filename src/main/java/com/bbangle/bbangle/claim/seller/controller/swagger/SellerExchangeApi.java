package com.bbangle.bbangle.claim.seller.controller.swagger;

import com.bbangle.bbangle.claim.seller.controller.dto.ExchangeDecisionRequest;
import com.bbangle.bbangle.claim.seller.controller.dto.ExchangeInvoiceRequest;
import com.bbangle.bbangle.claim.seller.controller.dto.ExchangeInvoiceUpdateRequest;
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

    @Operation(
        summary = "교환 재배송 운송장 입력",
        description = "APPROVED 상태의 교환 건에 재배송 택배사 코드와 운송장 번호를 등록하고, "
            + "상태를 교환 상품 발송(EXCHANGE_ITEM_SHIPPED)으로 변경한다."
    )
    CommonResult registerExchangeInvoice(
        @Parameter(description = "교환 요청 ID") Long exchangeId,
        ExchangeInvoiceRequest request,
        @Parameter(hidden = true) Long sellerId
    );

    @Operation(
        summary = "교환 재배송 운송장 수정",
        description = "운송장 등록 완료(RESHIPPED) 상태일 때만 택배사 코드와 운송장 번호를 수정할 수 있다. "
            + "수정 사유(reason)가 제공된 경우 교환 엔티티의 코멘트 필드에 기록된다."
    )
    CommonResult updateExchangeInvoice(
        @Parameter(description = "교환 요청 ID") Long exchangeId,
        ExchangeInvoiceUpdateRequest request,
        @Parameter(hidden = true) Long sellerId
    );
}
