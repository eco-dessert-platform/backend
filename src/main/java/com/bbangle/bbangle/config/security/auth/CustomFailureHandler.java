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
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final String DEFAULT_OAUTH_PAGE = "/oauth.html";
    private final SlackAdaptor slackAdaptor;
    private final OAuth2HandlerProperties oauth2HandlerProperties;
    private final OAuth2StateParser stateParser;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        BbangleErrorCode code = extractErrorCode(exception);

        try {
            OAuthParams dto = stateParser.getParams(request.getParameter("state"), getClass());
            handleLoggingAndAlert(request, exception, code);
            redirectToErrorPage(request, response, dto, code);
        } catch (OAuth2Exception e) {
            log.error("State parameter parsing failed", e);
            slackAdaptor.sendAlert(request, e);
            redirectToDefault(request, response, e);
        } catch (Exception e) {
            log.error("Unexpected exception in FailureHandler", e);
            slackAdaptor.sendAlert(request, e);
            redirectToDefault(request, response, null);
        }
    }

    private BbangleErrorCode extractErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2Exception ex) return ex.getCode();
        return null;
    }

    private void handleLoggingAndAlert(
        HttpServletRequest request,
        AuthenticationException exception,
        BbangleErrorCode code
    ) {
        if (code != null && code.getHttpStatus().is5xxServerError()) {  // 서버 내부 에러
            log.error("OAuth2 Authentication Failed [{}]: {}", code, exception.getMessage(), exception);
            slackAdaptor.sendAlert(request, exception);
        } else if (code != null) {  // 클라이언트/인증 에러
            log.warn("OAuth2 Authentication Failed: [{}] {}", code, code.getMessage());
        } else {    // 예기치 못한 내부 오류
            log.error("Unexpected authentication error in FailureHandler | class: {} | message: {} | cause: {}",
                exception.getClass().getName(),
                exception.getMessage(),
                exception.getCause(),
                exception
            );
            slackAdaptor.sendAlert(request, exception);
        }
    }

    private void redirectToDefault(
        HttpServletRequest request,
        HttpServletResponse response,
        OAuth2Exception e
    ) throws IOException {
        String url = (e == null) ? DEFAULT_OAUTH_PAGE : DEFAULT_OAUTH_PAGE + "?error=" + e.getCode().name();
        getRedirectStrategy().sendRedirect(request, response, url);
    }

    private void redirectToErrorPage(
        HttpServletRequest request,
        HttpServletResponse response,
        OAuthParams params,
        BbangleErrorCode code
    ) throws IOException {
        if (params == null) {
            getRedirectStrategy().sendRedirect(request, response, DEFAULT_OAUTH_PAGE + "?error=" + code.name());
        } else {
            getRedirectStrategy().sendRedirect(request, response, oauth2HandlerProperties.getErrorUrl(code, params));
        }
    }
}
