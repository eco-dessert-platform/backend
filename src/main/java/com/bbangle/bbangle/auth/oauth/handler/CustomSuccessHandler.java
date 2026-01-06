package com.bbangle.bbangle.auth.oauth.handler;

import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
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

// TODO : Test
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofMinutes(5);
    // TODO : .env 환경변수로 분리
    public static final String REDIRECT_URL = "http://localhost:8000/callback/social";

    private final TokenProvider tokenProvider;
    private final RedisRepository redisRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        CustomUserDetails oAuth2User = (CustomUserDetails) authentication.getPrincipal();
        String refreshToken = tokenProvider.generateToken(oAuth2User.id(), oAuth2User.role(), REFRESH_TOKEN_DURATION);
        UUID uuid = UUID.randomUUID();

        try {
            redisRepository.setFromString(
                    "oauth2:code",
                    uuid.toString(),
                    refreshToken,
                    REFRESH_TOKEN_TTL
            );
        } catch (Exception e) {
            throw new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR, e);
        }

        response.sendRedirect(createRedirectUrl(uuid));
    }

    String createRedirectUrl(UUID uuid) {
        return REDIRECT_URL + "?generateToken=" + uuid;
    }
}
