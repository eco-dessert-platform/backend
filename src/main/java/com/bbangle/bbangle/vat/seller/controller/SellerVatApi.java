package com.bbangle.bbangle.vat.seller.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.vat.seller.controller.dto.request.SellerVatSearchRequest;
import com.bbangle.bbangle.vat.seller.controller.dto.response.SellerVatSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;

@Tag(name = "Seller Vat", description = "판매자 부가세 신고 내역 API")
public interface SellerVatApi {

    @Operation(
        summary = "판매자 부가세 신고 내역 조회",
        description = "정산일 기준 정산 데이터를 월별로 합산해 부가세 신고 내역을 조회합니다. 현재 Toss 연동 전까지 임시 데이터를 사용합니다."
    )
    SingleResult<SellerVatSummaryResponse> getSellerVatSummary(
        @ParameterObject SellerVatSearchRequest request,
        @Parameter(hidden = true) Long sellerId
    );
}
