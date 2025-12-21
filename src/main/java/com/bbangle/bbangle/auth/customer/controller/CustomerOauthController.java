package com.bbangle.bbangle.auth.customer.controller;

import com.bbangle.bbangle.auth.customer.controller.swagger.CustomerOauthApi;
import com.bbangle.bbangle.auth.customer.service.CustomerOauthService;
import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.kakao.dto.LoginTokenResponse;
import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
public class CustomerOauthController implements CustomerOauthApi {

    private final CustomerOauthService customerOauthService;
    private final ResponseService responseService;

    @Override
    @GetMapping("/api/v1/oauth/login/{oauthServerType}")
    public CommonResult login(
            @PathVariable("oauthServerType")
            OauthServerType oauthServerType,
            @RequestParam("token")
            String token
    ) {
        LoginTokenResponse loginTokenResponse = customerOauthService.login(oauthServerType, token);
        return responseService.getSingleResult(loginTokenResponse);
    }

}
