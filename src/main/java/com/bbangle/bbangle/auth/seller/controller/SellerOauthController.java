package com.bbangle.bbangle.auth.seller.controller;

import static com.bbangle.bbangle.util.CookieUtils.createCookie;

import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenRequest;
import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenResponse;
import com.bbangle.bbangle.auth.seller.controller.swagger.SellerOauthApi;
import com.bbangle.bbangle.auth.seller.facade.OAuth2SellerFacade;
import com.bbangle.bbangle.auth.seller.facade.dto.GenerateTokenDTO;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
    @PostMapping("/tokens")
    public SingleResult<GenerateTokenResponse> sellerToken(
        @RequestBody @Valid GenerateTokenRequest request,
        HttpServletResponse response
    ) {
        GenerateTokenDTO dto = oAuth2SellerFacade.generateToken(request.generateToken());

        response.setHeader("Authorization", "Bearer " + dto.accessToken());
        response.addHeader(HttpHeaders.SET_COOKIE, createCookie(dto.refreshToken(), TokenProvider.REFRESH_TOKEN_DURATION).toString());

        return responseService.getSingleResult(
            GenerateTokenResponse.builder()
                .sellerId(dto.sellerId())
                .status(dto.status())
                .build()
        );
    }
}
