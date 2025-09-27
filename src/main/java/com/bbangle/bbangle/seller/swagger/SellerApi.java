package com.bbangle.bbangle.seller.swagger;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.seller.dto.SellerAccountUpdateRequest;
import com.bbangle.bbangle.seller.dto.SellerStoreNameUpdateRequest;
import com.bbangle.bbangle.seller.dto.SellerUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Sellers", description = "판매자 관리 API")
public interface SellerApi {

    @Operation(
        summary = "판매자 정보 수정",
        description = "기존 판매자 정보를 전체 수정합니다."
    )
    CommonResult updateSeller(
        @RequestBody SellerUpdateRequest request,
        @AuthenticationPrincipal Long sellerId
    );

    @Operation(
        summary = "스토어명 변경",
        description = "스토어명을 변경합니다. (최초 1회만 가능)"
    )
    CommonResult updateStoreName(
        @RequestBody SellerStoreNameUpdateRequest request,
        @AuthenticationPrincipal Long sellerId
    );

    @Operation(
        summary = "계좌 정보 변경",
        description = "판매자 계좌 정보를 변경합니다."
    )
    CommonResult updateAccount(
        @RequestBody SellerAccountUpdateRequest request,
        @AuthenticationPrincipal Long sellerId
    );
}