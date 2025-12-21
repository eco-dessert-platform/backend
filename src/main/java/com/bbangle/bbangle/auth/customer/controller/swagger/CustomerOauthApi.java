package com.bbangle.bbangle.auth.customer.controller.swagger;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.common.dto.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Customer Oauth Login", description = "(고객) 로그인 Oauth API")
public interface CustomerOauthApi {

    @Operation(summary = "Oauth 로그인")
    CommonResult login(
            @Parameter(description = "Oauth 서비스 종류", example = "KAKAO, GOOGLE")
            @PathVariable("oauthServerType")
            OauthServerType oauthServerType,
            @Parameter(description = "Oauth 토큰")
            @RequestParam("token")
            String token
    );

}
