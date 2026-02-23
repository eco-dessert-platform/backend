package com.bbangle.bbangle.auth.oauth.client.controller;

import static com.bbangle.bbangle.util.CookieUtils.createCookie;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.controller.swagger.OAuthApi;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.ProfileType;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.UserType;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenResponse;
import com.bbangle.bbangle.auth.oauth.client.facade.OAuthFacade;
import com.bbangle.bbangle.auth.oauth.client.service.OAuthService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.PublicApiPath;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("")
public class OAuthController implements OAuthApi {

    private final ResponseService responseService;
    private final OAuthFacade oAuthFacade;
    private final OAuthService oAuthService;

    @Override
    @GetMapping(PublicApiPath.OAUTH_PREFIX + "/{oauthServerType}")
    public void login(
        @PathVariable OauthServerType oauthServerType,
        UserType userType,
        ProfileType profileType
    ) {}

    @Override
    @PostMapping(PublicApiPath.AUTH_PREFIX + "/reissue")
    public CommonResult reissueToken(
        @CookieValue(value = "refreshToken", required = false)
        String refreshToken,
        HttpServletResponse response
    ) {
        if (refreshToken == null) throw new BbangleException(BbangleErrorCode._UNAUTHORIZED);

        TokenResponse dto = oAuthFacade.reissueToken(refreshToken);
        response.setHeader("Authorization", "Bearer " + dto.accessToken());
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie(dto.refreshToken(), TokenProvider.REFRESH_TOKEN_DURATION).toString());

        return responseService.getSuccessResult();
    }

    @Override
    @DeleteMapping(PublicApiPath.AUTH_PREFIX + "/logout")
    public CommonResult logout(
        @CookieValue(value = "refreshToken", required = false)
        String refreshToken,
        HttpServletResponse response
    ) {
        if (refreshToken != null) oAuthService.deleteRefreshToken(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie("", Duration.ZERO).toString());

        return responseService.getSuccessResult();
    }
}
