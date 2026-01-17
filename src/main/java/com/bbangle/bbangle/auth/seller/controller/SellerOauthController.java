package com.bbangle.bbangle.auth.seller.controller;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenResponse;
import com.bbangle.bbangle.auth.seller.controller.swagger.SellerOauthApi;
import com.bbangle.bbangle.auth.seller.facade.OAuth2SellerFacade;
import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    @GetMapping("/tokens")
    public SingleResult<GenerateTokenResponse> sellerToken(
        @RequestParam(value = "generateToken", defaultValue = "") String code
    ) {
        if (code.isBlank()) throw new BbangleException(BbangleErrorCode._BAD_REQUEST);
        // TODO : Test
        return responseService.getSingleResult(oAuth2SellerFacade.generateToken(code));
    }
}
