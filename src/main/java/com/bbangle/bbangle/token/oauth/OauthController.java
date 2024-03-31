package com.bbangle.bbangle.token.oauth;

import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import com.bbangle.bbangle.token.oauth.infra.kakao.dto.LoginTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth")
@RestController
public class OauthController {

    private final OauthService oauthService;

    @GetMapping("/login/{oauthServerType}")
    ResponseEntity<LoginTokenResponse> login(
        @PathVariable
        OauthServerType oauthServerType,
        @RequestParam("token")
        String token
    ) {
        LoginTokenResponse loginTokenResponse = oauthService.login(oauthServerType, token);
        return ResponseEntity.ok(loginTokenResponse);
    }

}
