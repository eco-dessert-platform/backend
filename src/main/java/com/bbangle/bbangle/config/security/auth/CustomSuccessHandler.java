package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2InfoRedisDTO;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final Duration TEMP_CODE_TTL = Duration.ofMinutes(5);
    public static final String OAUTH_CODE_NAMESPACE = "oauth2:code";

    private final OAuth2HandlerProperties oauth2HandlerProperties;
    private final RedisRepository redisRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        CustomUserDetails oAuth2User = (CustomUserDetails) authentication.getPrincipal();
        UUID uuid = UUID.randomUUID();

        try {
            redisRepository.setFromDTO(
                OAUTH_CODE_NAMESPACE,
                uuid.toString(),
                OAuth2InfoRedisDTO.builder()
                    .id(oAuth2User.id())
                    .role(oAuth2User.role())
                    .status(oAuth2User.status())
                    .build(),
                TEMP_CODE_TTL
            );
        } catch (Exception e) {
            throw new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR, e);
        }

        response.sendRedirect(createRedirectUrl(uuid));
    }

    private String createRedirectUrl(UUID uuid) {
        return oauth2HandlerProperties.success() + "?generateToken=" + uuid;
    }
}
