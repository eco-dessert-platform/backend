package com.bbangle.bbangle.auth.customer.controller;

import com.bbangle.bbangle.auth.customer.controller.dto.CreateAccessTokenRequest;
import com.bbangle.bbangle.auth.customer.controller.dto.CreateAccessTokenResponse;
import com.bbangle.bbangle.auth.customer.service.CustomerTokenService;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CustomerTokenApiController {

    private final CustomerTokenService customerTokenService;
    private final ResponseService responseService;
    private final TokenProvider tokenProvider;

    @PostMapping("/api/v1/token")
    @Operation(summary = "새로운 엑세스 토큰 발급", description = "쿠키로 엑세스 토큰 받고 쿠키로 전달 / 쿠키 헤더: Set-Cookie, 쿠키명: access_token")
    public CommonResult createNewAccessToken(
            @RequestBody
            CreateAccessTokenRequest request
    ) {
        if (!tokenProvider.isValidToken(request.getRefreshToken())) {
            return responseService.getSuccessResult("Unexpected token", 0);
        }

        String newAccessToken = customerTokenService.createNewAccessToken(request.getRefreshToken());

        return responseService.getSingleResult(new CreateAccessTokenResponse(newAccessToken));
    }

}
