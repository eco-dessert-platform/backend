package com.bbangle.bbangle.auth.oauth.handler;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomFailureHandler implements AuthenticationFailureHandler {

    public static final String REDIRECT_ERROR_URL = "http://localhost:8000/callback/social?error=";

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        if (exception instanceof OAuth2Exception ex) {
            BbangleErrorCode code = ex.getCode();

            if (code.getHttpStatus().is5xxServerError()) {
                log.error("OAuth2 server error", ex);
            } else {
                log.warn("OAuth2 Authentication Failed: [{}] {}", code, code.getMessage());
            }

            response.sendRedirect(REDIRECT_ERROR_URL + code + "&code=" + code.getCode());
            return;
        }

        log.warn("Unknown authentication error", exception);
        response.sendRedirect(REDIRECT_ERROR_URL + "UNKNOWN_ERROR");
    }
}
