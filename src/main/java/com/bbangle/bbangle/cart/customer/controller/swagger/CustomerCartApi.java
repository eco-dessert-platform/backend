package com.bbangle.bbangle.cart.customer.controller.swagger;

import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest;
import com.bbangle.bbangle.common.dto.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Customer Cart", description = "(커스토머) 장바구니 API")
public interface CustomerCartApi {

    @Operation(
        summary = "(커스토머) 장바구니 상품 추가",
        description = "장바구니에 선택한 상품을 추가합니다."
    )
    CommonResult addCart(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody CartRequest.AddCartRequest request
    );

    @Operation(
        summary = "(커스토머) 장바구니 상품 삭제",
        description = "장바구니에서 선택한 상품 옵션을 삭제합니다."
    )
    CommonResult deleteCartOptions(
        @AuthenticationPrincipal Long memberId,
        @Valid @RequestBody CartRequest.DeleteCartOptionsRequest request
    );
}
