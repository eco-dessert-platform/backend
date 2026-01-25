package com.bbangle.bbangle.auth.seller.controller;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenResponse;
import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenRequest;
import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenResponse;
import com.bbangle.bbangle.auth.seller.controller.swagger.SellerOauthApi;
import com.bbangle.bbangle.auth.seller.facade.OAuth2SellerFacade;
import com.bbangle.bbangle.auth.seller.service.OAuthSellerService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(SellerApiPath.PREFIX + "/oauth2")
public class SellerOauthController implements SellerOauthApi {

    private final ResponseService responseService;
    private final OAuth2SellerFacade oAuth2SellerFacade;
    private final OAuthSellerService oAuthSellerService;

    @Override
    @GetMapping("/authorization/{oauthServerType}")
    public void sellerLogin(
        @PathVariable("oauthServerType") OauthServerType oauthServerType
    ) {}

    @Override
    @PostMapping("/tokens")
    public SingleResult<GenerateTokenResponse> sellerToken(
        @RequestBody @Valid GenerateTokenRequest request
    ) {
        return responseService.getSingleResult(
            oAuth2SellerFacade.generateToken(request.generateToken())
        );
    }

    @Override
    @PostMapping("/reissue")
    public CommonResult reissueToken(
        @CookieValue(value = "refreshToken", required = false)
        String refreshToken,
        HttpServletResponse response
    ) {
        if (refreshToken == null) throw new BbangleException(BbangleErrorCode._UNAUTHORIZED);

        TokenResponse dto = oAuth2SellerFacade.reissueToken(refreshToken);
        response.setHeader("Authorization", "Bearer " + dto.accessToken());
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie(dto.refreshToken(), Duration.ofDays(14)).toString());

        return responseService.getSuccessResult();
    }

    @Override
    @DeleteMapping("/logout")
    public CommonResult logout(
        @CookieValue(value = "refreshToken", required = false)
        String refreshToken,
        HttpServletResponse response
    ) {
        if (refreshToken != null) oAuthSellerService.logout(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie("", Duration.ofMillis(1)).toString());

        return responseService.getSuccessResult();
    }


    private ResponseCookie createCookie(String value, Duration duration) {
        return ResponseCookie.from("refreshToken", value)
            .httpOnly(true)
            .secure(true)  // TODO : 로컬에서 사용할 때는 주석처리
            .path("/")
            .maxAge(duration)
            .sameSite("Strict")
            .build();
    }
}
