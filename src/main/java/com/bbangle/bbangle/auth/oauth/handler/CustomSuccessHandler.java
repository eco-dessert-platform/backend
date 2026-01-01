package com.bbangle.bbangle.auth.oauth.handler;

import com.bbangle.bbangle.auth.oauth.dto.CustomUserDetails;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofSeconds(10);

    private final TokenProvider tokenProvider;
    private final RedisRepository redisRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        CustomUserDetails oAuth2User = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = oAuth2User.id();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority grantedAuthority = iterator.next();
        String role = grantedAuthority.getAuthority();

        String refreshToken = tokenProvider.generateToken(memberId, Role.from(role), REFRESH_TOKEN_DURATION);
        UUID uuid = UUID.randomUUID();

        redisRepository.setFromString(
                "generateToken",
                uuid.toString(),
                refreshToken,
                REFRESH_TOKEN_TTL
        );

        // TODO : 리다이렉트 URL 변경하기
        response.sendRedirect("http://localhost:8000/callback/social?generateToken=" + uuid);
    }
}
