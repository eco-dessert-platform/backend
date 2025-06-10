package com.bbangle.bbangle.token.oauth;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.token.jwt.TokenCookieProvider;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import com.bbangle.bbangle.token.oauth.infra.kakao.dto.LoginTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth")
@RestController
public class OauthController {

    private final OauthService oauthService;
    private final ResponseService responseService;
    private final TokenCookieProvider tokenCookieProvider;

    @GetMapping("/login/{oauthServerType}")
    @Operation(summary = "Oauth 로그인")
    CommonResult login(
        @PathVariable("oauthServerType") OauthServerType oauthServerType,
        @RequestParam String tokenByOauth,
        HttpServletResponse response
    ) {
        LoginTokenResponse loginTokenResponse = oauthService.login(oauthServerType, tokenByOauth);
        
        tokenCookieProvider.addAccessTokenCookie(response, loginTokenResponse.accessToken());
        tokenCookieProvider.addRefreshTokenCookie(response, loginTokenResponse.refreshToken());

        return responseService.getSuccessResult("Login successful", 1);
    }
}
