package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class OAuth2ClientValidationFilter extends OncePerRequestFilter {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2HandlerProperties oauth2HandlerProperties;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (uri.contains("/oauth2/authorization/")) {
            if (!validateClientRegistration(uri, request, response)) return;
            if (!validateOAuth2Params(request, response)) return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean validateClientRegistration(
        String uri,
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        String registrationId = uri.substring(uri.lastIndexOf("/") + 1);
        ClientRegistration client = clientRegistrationRepository.findByRegistrationId(registrationId);

        if (client == null) {
            BbangleErrorCode code = BbangleErrorCode.NOT_SUPPORTED_SERVER;
            log.warn("Client Registration Id: [{}] - {}", registrationId, code);
            redirectStrategy.sendRedirect(request, response, createRedirectUrl(request, code));

            return false;
        }
        return true;
    }

    private boolean validateOAuth2Params(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws IOException {
        String user = request.getParameter("user");
        String profile = request.getParameter("profile");

        OAuth2DTO.OAuthParams dto = OAuth2DTO.OAuthParams.builder()
            .user(user)
            .profile(profile)
            .build();

        if (!dto.valid()) {
            BbangleErrorCode code = BbangleErrorCode.INVALID_OAUTH_PARAMS;
            log.warn("Invalid OAuth2 parameters: {} - {}", dto, code);
            redirectStrategy.sendRedirect(request, response, createRedirectUrl(request, code));

            return false;
        }
        return true;
    }

    private String createRedirectUrl(HttpServletRequest request, BbangleErrorCode code) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        StringBuilder domain = new StringBuilder(scheme + "://" + serverName);

        // [80 | 443] 포트가 아닌 경우에만 포트 붙이기
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            domain.append(":").append(serverPort);
        }

        return domain + "/" + oauth2HandlerProperties.redirect().error() + "?error=" + code + "&code=" + code.getCode();
    }
}
