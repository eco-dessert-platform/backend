package com.bbangle.bbangle.auth.seller.controller;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.seller.controller.swagger.SellerOauthApi;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SellerOauthController implements SellerOauthApi {

    private final ResponseService responseService;

    @Override
    @GetMapping(SellerApiPath.PREFIX + "/oauth2/authorization/{oauthServerType}")
    public void sellerLogin(
        @PathVariable("oauthServerType")
        OauthServerType oauthServerType,
        HttpServletResponse response
    ) {}

}
