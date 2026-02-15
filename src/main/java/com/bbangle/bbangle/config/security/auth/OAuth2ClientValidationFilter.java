package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class OAuth2ClientValidationFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_USERS = new HashSet<>(List.of("customer", "seller"));
    private static final Set<String> ALLOWED_PROFILES = new HashSet<>(List.of("local", "prod"));
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String prefix = SellerApiPath.PREFIX + "/oauth2/authorization/";

        if (uri.startsWith(prefix)) {
            String registrationId = uri.substring(prefix.length());
            ClientRegistration client = clientRegistrationRepository.findByRegistrationId(registrationId);
            String user = request.getParameter("user");
            String profile = request.getParameter("profile");

            if (client == null) {
                log.warn("Client Registration Id: [{}] - {}", registrationId, BbangleErrorCode.NOT_SUPPORTED_SERVER);
                response.sendRedirect(createRedirectUrl(BbangleErrorCode.NOT_SUPPORTED_SERVER));
                return;
            }

            if (!validate(user, profile)) {
                log.warn("Invalid OAuth2 Params: [{}] - {}", registrationId, BbangleErrorCode.OAUTH_INVALID_PARAMS);
                response.sendRedirect(createRedirectUrl(BbangleErrorCode.OAUTH_INVALID_PARAMS));
                return;
            }

            request.setAttribute("oauth_user", user);
            request.setAttribute("oauth_profile", profile);
        }

        filterChain.doFilter(request, response);
    }

    private String createRedirectUrl(BbangleErrorCode code) {
        return "/oauth.html?error=" + code.name();
    }

    private boolean validate(String user, String profile) {
        return ALLOWED_USERS.contains(user) && ALLOWED_PROFILES.contains(profile);
    }
}
