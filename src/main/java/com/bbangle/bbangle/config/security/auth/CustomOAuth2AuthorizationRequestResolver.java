package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2Redis;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.config.security.SellerApiPath;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    public static final Duration OAUTH_STATE_TTL = Duration.ofMinutes(3);
    public static final String OAUTH_STATE_NAMESPACE = "oauth2:params";

    private final OAuth2AuthorizationRequestResolver defaultResolver;
    private final RedisRepository redisRepository;

    public CustomOAuth2AuthorizationRequestResolver(
        ClientRegistrationRepository clientRegistrationRepository,
        RedisRepository redisRepository
    ) {
        this.defaultResolver =
            new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                SellerApiPath.PREFIX + "/oauth2/authorization"
            );
        this.redisRepository = redisRepository;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest =
            defaultResolver.resolve(request);

        return saveParamsAndReturn(authorizationRequest, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest =
            defaultResolver.resolve(request, clientRegistrationId);

        return saveParamsAndReturn(authorizationRequest, request);
    }

    private OAuth2AuthorizationRequest saveParamsAndReturn(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request
    ) {
        if (authorizationRequest == null) return null;

        String state = authorizationRequest.getState();
        String user = (String) request.getAttribute("oauth_user");
        String profile = (String) request.getAttribute("oauth_profile");

        if (user == null || profile == null) {
            return authorizationRequest;
        }

        OAuth2Redis.OAuthParams dto = OAuth2Redis.OAuthParams.builder()
            .user(user)
            .profile(profile)
            .build();

        redisRepository.setFromDTO(OAUTH_STATE_NAMESPACE, state, dto, OAUTH_STATE_TTL);

        return authorizationRequest;
    }
}
