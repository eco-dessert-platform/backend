package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.OAuthParams;
import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final SlackAdaptor slackAdaptor;
    private final OAuth2HandlerProperties oauth2HandlerProperties;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        OAuthParams dto = OAuth2DTO.getParams(request, getClass());

        BbangleErrorCode code = extractErrorCode(exception);
        handleLoggingAndAlert(request, exception, code);

        getRedirectStrategy().sendRedirect(request, response, createRedirectUrl(dto, code));
    }

    private BbangleErrorCode extractErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2Exception ex) return ex.getCode();
        return null;
    }

    private void handleLoggingAndAlert(HttpServletRequest request, AuthenticationException exception, BbangleErrorCode code) {
        if (code != null && code.getHttpStatus().is5xxServerError()) {
            log.error(exception.getMessage(), exception);
            slackAdaptor.sendAlert(request, exception);
        } else if (code != null) {
            log.warn("OAuth2 Authentication Failed: [{}] {}", code, code.getMessage());
        } else {
            log.error("Authentication error occurred - FailureHandler | class : {} | message : {} | cause : {}",
                exception.getClass().getName(),
                exception.getMessage(),
                exception.getCause(),
                exception
            );
            slackAdaptor.sendAlert(request, exception);
        }
    }

    private String createRedirectUrl(OAuthParams params, BbangleErrorCode code) {
        return oauth2HandlerProperties.getErrorUrl(code, params);
    }
}
