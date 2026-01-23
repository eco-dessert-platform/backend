package com.bbangle.bbangle.auth.seller.controller;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenResponse;
import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenRequest;
import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenResponse;
import com.bbangle.bbangle.auth.seller.controller.swagger.SellerOauthApi;
import com.bbangle.bbangle.auth.seller.facade.OAuth2SellerFacade;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

    // TODO : Test
    @Override
    @PostMapping("/reissue")
    public SingleResult<String> reissueToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) {

        // 쿠키에서 Refresh Token 추출
        String refresh = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
            .filter(cookie -> "refreshToken".equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElseThrow(() -> new BbangleException(BbangleErrorCode._UNAUTHORIZED));

        // Token 생성
        TokenResponse dto = oAuth2SellerFacade.reissueToken(refresh);
        response.setHeader("Authorization", "Bearer " + dto.accessToken());
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie(dto.refreshToken()).toString());

        return responseService.getSingleResult("새로운 토큰 발급");
    }

    private ResponseCookie createCookie(String value) {
        return ResponseCookie.from("refreshToken", value)
            .httpOnly(true)
            //.secure(true)  // 로컬에서 사용할 때는 주석처리
            .path("/")
            .maxAge(Duration.ofDays(14))
            .sameSite("Strict")
            .build();
    }
}
