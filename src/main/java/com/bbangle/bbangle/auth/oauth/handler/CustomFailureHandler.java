package com.bbangle.bbangle.auth.oauth.handler;

import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomFailureHandler implements AuthenticationFailureHandler {

    @Value("${oauth2.redirect.error}")
    public static String REDIRECT_URL;
    private final SlackAdaptor slackAdaptor;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        if (exception instanceof OAuth2Exception ex) {
            BbangleErrorCode code = ex.getCode();

            if (code.getHttpStatus().is5xxServerError()) {
                log.error(ex.getMessage(), ex);
                slackAdaptor.sendAlert(request, ex);
            } else {
                log.warn("OAuth2 Authentication Failed: [{}] {}", code, code.getMessage());
            }

            response.sendRedirect(createRedirectUrl(code));
            return;
        }

        log.warn("Unknown authentication error - [{}] {}", exception.getCause(), exception.getMessage());
        response.sendRedirect(createRedirectUrl(null));
    }

    String createRedirectUrl(BbangleErrorCode code) {
        if (code == null) return REDIRECT_URL + "?error=" + "UNKNOWN_ERROR";
        return REDIRECT_URL + "?error=" + code + "&code=" + code.getCode();
    }
}
