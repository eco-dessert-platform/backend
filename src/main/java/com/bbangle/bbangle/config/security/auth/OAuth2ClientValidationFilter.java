package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class OAuth2ClientValidationFilter extends OncePerRequestFilter {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final CustomFailureHandler failureHandler;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();

        if (uri.contains("/oauth2/authorization/")) {
            String registrationId = uri.substring(uri.lastIndexOf("/") + 1);
            ClientRegistration client = clientRegistrationRepository.findByRegistrationId(registrationId);

            if (client == null) {
                log.warn("Client Registration Id: [{}] - {}", registrationId, BbangleErrorCode.NOT_SUPPORTED_SERVER);

                failureHandler.onAuthenticationFailure(
                    request,
                    response,
                    new OAuth2Exception(BbangleErrorCode.NOT_SUPPORTED_SERVER)
                );

                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
