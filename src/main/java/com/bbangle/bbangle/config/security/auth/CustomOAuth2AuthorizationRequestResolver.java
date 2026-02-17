package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.config.security.SellerApiPath;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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

    private OAuth2AuthorizationRequest saveParamsAndReturn(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request
    ) {
        if (authorizationRequest == null) return null;

        String user = request.getParameter("user");
        String profile = request.getParameter("profile");
        if (user == null || profile == null) return authorizationRequest;

        String csrfState = authorizationRequest.getState();
        String encodedUser = Base64.getUrlEncoder().encodeToString(user.getBytes(StandardCharsets.UTF_8));
        String encodedProfile = Base64.getUrlEncoder().encodeToString(profile.getBytes(StandardCharsets.UTF_8));

        String state = csrfState + "|" + encodedUser + "|" + encodedProfile;

        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .state(state)
            .build();
    }
}