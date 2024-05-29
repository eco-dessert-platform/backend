package com.bbangle.bbangle.token.controller;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.token.dto.CreateAccessTokenRequest;
import com.bbangle.bbangle.token.dto.CreateAccessTokenResponse;
import com.bbangle.bbangle.token.jwt.TokenProvider;
import com.bbangle.bbangle.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenApiController {

    private final TokenService tokenService;
    private final ResponseService responseService;
    private final TokenProvider tokenProvider;

    @PostMapping("/api/v1/token")
    public CommonResult createNewAccessToken(
        @RequestBody
        CreateAccessTokenRequest request
    ) {
        if (!tokenProvider.isValidToken(request.getRefreshToken())) {
            return responseService.getSuccessResult("Unexpected token", 0);
        }

        String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

        return responseService.getSingleResult(new CreateAccessTokenResponse(newAccessToken));
    }

}
