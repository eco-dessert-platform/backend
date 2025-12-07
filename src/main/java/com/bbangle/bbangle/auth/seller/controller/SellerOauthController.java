package com.bbangle.bbangle.auth.seller.controller;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.seller.controller.swagger.SellerOauthApi;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SellerOauthController implements SellerOauthApi {

    private final ResponseService responseService;

    @Override
    @GetMapping("/api/v1/oauth/seller/login/{oauthServerType}")
    public CommonResult sellerLogin(
            @PathVariable("oauthServerType")
            OauthServerType oauthServerType,
            @RequestParam("token")
            String token
    ) {
        // TODO: 개발 필요
        return responseService.getSuccessResult();
    }

}
