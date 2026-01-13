package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final Duration TEMP_CODE_TTL = Duration.ofMinutes(5);

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
            redisRepository.setFromMap(
                "oauth2:code",
                uuid.toString(),
                createUserInfo(oAuth2User.id(), oAuth2User.role(), oAuth2User.status()),
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

    private Map<String, Object> createUserInfo(Long id, Role role, CertificationStatus status) {
        return Map.of(
            "id", id,
            "Role", role,
            "Status", status
        );
    }
}
