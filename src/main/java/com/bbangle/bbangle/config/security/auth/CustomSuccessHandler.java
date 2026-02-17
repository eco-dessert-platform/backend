package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.OAuth2StateParser;
import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.OAuthParams;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final Duration TEMP_CODE_TTL = Duration.ofMinutes(5);
    public static final String OAUTH_CODE_NAMESPACE = "oauth2:code";

    private final OAuth2HandlerProperties oauth2HandlerProperties;
    private final RedisRepository redisRepository;
    private final OAuth2StateParser stateParser;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuthParams dto = stateParser.getParams(request.getParameter(OAuth2ParameterNames.STATE));
        CustomUserDetails oAuth2User = (CustomUserDetails) authentication.getPrincipal();
        UUID uuid = generateCode(oAuth2User);

        getRedirectStrategy().sendRedirect(request, response, createRedirectUrl(dto, uuid));
    }

    private UUID generateCode(CustomUserDetails oAuth2User) {
        UUID uuid = UUID.randomUUID();
        try {
            redisRepository.setFromDTO(
                OAUTH_CODE_NAMESPACE,
                uuid.toString(),
                OAuth2DTO.InfoDTO.builder()
                    .id(oAuth2User.id())
                    .role(oAuth2User.role())
                    .status(oAuth2User.status())
                    .build(),
                TEMP_CODE_TTL
            );
        } catch (Exception e) {
            throw new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR, e);
        }
        return uuid;
    }

    private String createRedirectUrl(OAuthParams params, UUID uuid) {
        return oauth2HandlerProperties.getSuccessUrl(uuid, params);
    }
}
