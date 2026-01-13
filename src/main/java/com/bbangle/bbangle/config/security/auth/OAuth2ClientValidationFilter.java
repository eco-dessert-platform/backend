package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Profile("!test")
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2ClientValidationFilter extends OncePerRequestFilter {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuth2HandlerProperties oauth2HandlerProperties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (uri.contains("/oauth2/authorization/")) {
            String registrationId = uri.substring(uri.lastIndexOf("/") + 1);
            ClientRegistration client = clientRegistrationRepository.findByRegistrationId(registrationId);

            if (client == null) {
                log.warn("Client Registration Id: [{}] - {}", registrationId, BbangleErrorCode.NOT_SUPPORTED_SERVER);
                response.sendRedirect(createRedirectUrl());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String createRedirectUrl() {
        return oauth2HandlerProperties.error() + "?error=" + BbangleErrorCode.NOT_SUPPORTED_SERVER
            + "&code=" + BbangleErrorCode.NOT_SUPPORTED_SERVER.getCode();
    }
}
