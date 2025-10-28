package com.bbangle.bbangle.token.controller.swagger;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Oauth Login", description = "로그인 Oauth API")
public interface OauthApi {

    @Operation(summary = "Oauth 로그인")
    CommonResult login(
        @Parameter(description = "Oauth 서비스 종류", example = "KAKAO, GOOGLE")
        @PathVariable("oauthServerType")
        OauthServerType oauthServerType,
        @Parameter(description = "Oauth 토큰")
        @RequestParam("token")
        String token
    );

    @Operation(summary = "판매자 Oauth 로그인")
    CommonResult sellerLogin(
        @Parameter(description = "Oauth 서비스 종류", example = "KAKAO, GOOGLE")
        @PathVariable("oauthServerType")
        OauthServerType oauthServerType,
        @Parameter(description = "Oauth 토큰")
        @RequestParam("token")
        String token
    );

}
