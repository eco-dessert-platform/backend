package com.bbangle.bbangle.seller.controller.swagger;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.seller.controller.dto.SellerRequest_v1.SellerDocumentsRegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Seller", description = "(판매자) 판매자 API")
public interface SellerApi_v1 {

    @Operation(summary = "(판매자) 판매자 서류 등록")
    CommonResult registerDocuments(
        SellerDocumentsRegisterRequest request,
        Long memberId
    );

}
