package com.bbangle.bbangle.config.security.auth;

import com.bbangle.bbangle.auth.oauth.client.OAuth2StateParser;
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
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final SlackAdaptor slackAdaptor;
    private final OAuth2HandlerProperties oauth2HandlerProperties;
    private final OAuth2StateParser stateParser;

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException exception
    ) throws IOException, ServletException {

        OAuthParams oauthParams;
        try {
            oauthParams = stateParser.getParams(request.getParameter(OAuth2ParameterNames.STATE));
        } catch (OAuth2Exception e) {
            handleStateParsingFailure(request, response, e);
            return;
        }

        BbangleErrorCode errorCode = extractErrorCode(exception);
        handleLoggingAndAlert(request, exception, errorCode);
        sendRedirect(request, response, errorCode, oauthParams);
    }

    private void handleStateParsingFailure(
        HttpServletRequest request,
        HttpServletResponse response,
        OAuth2Exception e
    ) throws IOException {
        log.error("State Parsing failed", e);
        slackAdaptor.sendAlert(request, e);

        String targetUrl = oauth2HandlerProperties.getDefaultErrorUrl(e.getCode());
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void handleLoggingAndAlert(
        HttpServletRequest request,
        AuthenticationException exception,
        BbangleErrorCode code
    ) {
        if (code == null) {
            log.error("Unexpected Authentication Error: {}", exception.getMessage(), exception);
        } else if (code.getHttpStatus().is5xxServerError()) {
            log.error("OAuth2 Authentication Failed [{}]: {}", code, exception.getMessage(), exception);
        } else {
            log.warn("OAuth2 Authentication Failed: [{}] {}", code, code.getMessage());
            return;
        }

        slackAdaptor.sendAlert(request, exception);
    }

    private void sendRedirect(
        HttpServletRequest request,
        HttpServletResponse response,
        BbangleErrorCode code,
        OAuthParams params
    ) throws IOException {
        String targetUrl = oauth2HandlerProperties.getErrorUrl(code, params);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private BbangleErrorCode extractErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2Exception ex) return ex.getCode();
        return null;
    }
}
