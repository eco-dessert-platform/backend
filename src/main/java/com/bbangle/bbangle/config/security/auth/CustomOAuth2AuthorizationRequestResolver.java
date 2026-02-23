package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.config.security.SellerApiPath;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Slf4j
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(
        ClientRegistrationRepository clientRegistrationRepository
    ) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
            // TODO : OAuth 로그인 URL 변경하기
            clientRegistrationRepository, SellerApiPath.PREFIX + "/oauth2/authorization"
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return saveParamsAndReturn(authorizationRequest, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return saveParamsAndReturn(authorizationRequest, request);
    }

    // 프론트의 OAuth2 로그인 요청으로부터 파라미터를 추출하여 state 파라미터를 재생성하는 메서드
    private OAuth2AuthorizationRequest saveParamsAndReturn(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request
    ) {
        if (authorizationRequest == null) return null;

        // 1. OAuth2 파라미터 값 추출
        String user = request.getParameter("user");
        String profile = request.getParameter("profile");
        if (user == null) return authorizationRequest;

        // 2. profile 파라미터 값 2차 검증
        profile = OAuth2DTO.defaultProfile(profile);

        // 3. OAuth2 Redirect Filter에서 생성한 state에 프론트에서 요청한 파라미터를 덧붙임
        String state = authorizationRequest.getState() + "." + user + "." + profile;

        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .state(state)
            .build();
    }
}