package com.bbangle.bbangle.auth.seller.controller.swagger;

import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenRequest;
import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenResponse;
import com.bbangle.bbangle.common.dto.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Seller Oauth Login", description = "(판매자) 로그인 Oauth API")
public interface SellerOauthApi {

    @Operation(
        summary = "판매자 Token 발급 및 계정 상태 조회",
        description = """
        ### 판매자의 JWT 토큰을 발급 받고 계정 상태를 조회

        ---
        ### 판매자 계정 상태 설명 표
        |Status Code|설명|
        |-----------|----|
        |NEW|아직 사업자 정보를 **제출하지 않은 상태**|
        |PENDING|사업자 정보를 제출하였지만 **심사 중**인 상태|
        |APPROVED|관리자가 해당 사업자 정보를 **승인**한 상태|
        |REJECTED|부적합하다고 판단되어 사업자 정보를 **승인 거부**한 상태|
        """
    )
    SingleResult<GenerateTokenResponse> sellerToken(
        @RequestBody @Valid GenerateTokenRequest request,
        HttpServletResponse response
    );
}
