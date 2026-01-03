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

// TODO : Test
@Slf4j
@RequiredArgsConstructor
@Component
public class CustomFailureHandler implements AuthenticationFailureHandler {

    // TODO : .env 환경변수로 분리
    public static final String REDIRECT_URL = "http://localhost:8000/callback/social";

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

            response.sendRedirect(createRedirectUrl(code));
            return;
        }

        log.warn("Unknown authentication error", exception);
        response.sendRedirect(createRedirectUrl(null));
    }

    String createRedirectUrl(BbangleErrorCode code) {
        if (code == null) return REDIRECT_URL + "?error=" + "UNKNOWN_ERROR";
        return REDIRECT_URL + "?error=" + code + "&code=" + code.getCode();
    }
}
