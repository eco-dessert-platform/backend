package com.bbangle.bbangle.token.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.token.jwt.TokenCookieProvider;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import com.bbangle.bbangle.token.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenApiController {

    private final TokenService tokenService;
    private final ResponseService responseService;
    private final TokenProvider tokenProvider;
    private final TokenCookieProvider tokenCookieProvider;

    @PostMapping("/api/v1/token")
    public CommonResult createNewAccessToken(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String refreshToken = tokenCookieProvider.getRefreshTokenFromCookie(request);
        if (refreshToken == null || !tokenProvider.isValidToken(refreshToken)) {
            return responseService.getSuccessResult("Unexpected token", 0);
        }

        String newAccessToken = tokenService.createNewAccessToken(refreshToken);
        tokenCookieProvider.addAccessTokenCookie(response, newAccessToken);

        return responseService.getSuccessResult("Token refreshed successfully", 1);
    }
}
