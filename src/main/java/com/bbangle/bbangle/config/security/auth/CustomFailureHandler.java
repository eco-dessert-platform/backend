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

        // 1. state 파라미터로부터 값을 추출하여 OAuthParams DTO를 생성
        OAuthParams oauthParams;
        try {
            oauthParams = stateParser.getParams(request.getParameter(OAuth2ParameterNames.STATE));
        } catch (OAuth2Exception e) {
            // 1-1. State 값이 변조되었을 경우 서버 자체 HTML 파일을 출력
            handleStateParsingFailure(request, response, e);
            return;
        }

        // 2. BbangleErrorCode 추출
        BbangleErrorCode errorCode = extractErrorCode(exception);

        // 3. 예외 메세지 로그 출력 및 예외 종류에 따라 Slack에 알림
        handleLoggingAndAlert(request, exception, errorCode);

        // 4. OAuthParams 값에 따라 동적으로 리다이렉트
        sendRedirect(request, response, errorCode, oauthParams);
    }

    // State 파라미터 파싱 실패 시 서버 자체 HTML 파일을 출력하는 메서드
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

    // 로그 출력 및 Slack 알림 메서드
    private void handleLoggingAndAlert(
        HttpServletRequest request,
        AuthenticationException exception,
        BbangleErrorCode code
    ) {
        if (code == null) { // BbangleErrorCode가 아닌 경우 - Slack에 알림
            log.error("Unexpected Authentication Error: {}", exception.getMessage(), exception);
        } else if (code.getHttpStatus().is5xxServerError()) {   // 500번대 예외일 경우 - Slack에 알림
            log.error("OAuth2 Authentication Failed [{}]: {}", code, exception.getMessage(), exception);
        } else {    // 400번대 BbangleErrorCode 예외인 경우 로그만 출력
            log.warn("OAuth2 Authentication Failed: [{}] {}", code, code.getMessage());
            return;
        }

        slackAdaptor.sendAlert(request, exception);
    }

    // OAuth2 로그인 실패 시 리다이렉트할 URL 생성
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
