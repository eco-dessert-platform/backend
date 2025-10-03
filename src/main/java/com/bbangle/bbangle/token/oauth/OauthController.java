package com.bbangle.bbangle.token.oauth;

import com.bbangle.bbangle.common.dto.CommonResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import com.bbangle.bbangle.token.oauth.infra.kakao.dto.LoginTokenResponse;
import com.bbangle.bbangle.token.swagger.OauthApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/oauth")
public class OauthController implements OauthApi {

    private final OauthService oauthService;
    private final ResponseService responseService;

    @Override
    @GetMapping("/login/{oauthServerType}")
    public CommonResult login(
        @PathVariable("oauthServerType")
        OauthServerType oauthServerType,
        @RequestParam("token")
        String token
    ) {
        LoginTokenResponse loginTokenResponse = oauthService.login(oauthServerType, token);
        return responseService.getSingleResult(loginTokenResponse);
    }

    @Override
    @GetMapping("/seller/login/{oauthServerType}")
    public CommonResult sellerLogin(
        @PathVariable("oauthServerType")
        OauthServerType oauthServerType,
        @RequestParam("token")
        String token
    ) {
        // TODO: 개발 필요
        LoginTokenResponse loginTokenResponse = oauthService.login(oauthServerType, token);
        return responseService.getSingleResult(loginTokenResponse);
    }

}
