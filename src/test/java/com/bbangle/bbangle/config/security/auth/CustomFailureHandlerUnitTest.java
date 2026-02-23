package com.bbangle.bbangle.config.security.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.auth.oauth.client.OAuth2StateParser;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.OAuthParams;
import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;

@DisplayName("[단위테스트] CustomFailureHandler")
@ExtendWith(MockitoExtension.class)
class CustomFailureHandlerUnitTest {

    @Mock
    SlackAdaptor slackAdaptor;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    OAuth2HandlerProperties oauth2HandlerProperties;

    @Mock
    OAuth2StateParser stateParser;

    CustomFailureHandler customFailureHandler;

    @BeforeEach
    void setUp() {
        given(response.encodeRedirectURL(anyString())).willAnswer(invocation -> invocation.getArgument(0));
        given(request.getContextPath()).willReturn("");
        customFailureHandler = new CustomFailureHandler(slackAdaptor, oauth2HandlerProperties, stateParser);
    }

    @Test
    @DisplayName("5xx 에러 발생 시 Slack에 알림 전송 후 Redirect한다.")
    void failure_5xx_error() throws Exception {

        // given
        OAuth2Exception exception = new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR);
        OAuthParams params = mock(OAuthParams.class);

        given(request.getParameter(OAuth2ParameterNames.STATE)).willReturn("valid-state");
        given(stateParser.getParams("valid-state")).willReturn(params);

        given(oauth2HandlerProperties.getErrorUrl(BbangleErrorCode.INTERNAL_SERVER_ERROR, params))
            .willReturn("https://test.com/error");

        // when
        customFailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        verify(slackAdaptor).sendAlert(request, exception);
        verify(response).sendRedirect("https://test.com/error");
    }

    @Test
    @DisplayName("4xx 에러 발생 시 Slack에 알림 없이 Redirect한다.")
    void failure_4xx_error() throws Exception {

        // given
        OAuth2Exception exception = new OAuth2Exception(BbangleErrorCode.NOT_SUPPORTED_SERVER);
        OAuthParams params = mock(OAuthParams.class);

        given(request.getParameter(OAuth2ParameterNames.STATE)).willReturn("valid-state");
        given(stateParser.getParams("valid-state")).willReturn(params);

        given(oauth2HandlerProperties.getErrorUrl(BbangleErrorCode.NOT_SUPPORTED_SERVER, params))
            .willReturn("https://test.com/error");

        // when
        customFailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        verify(slackAdaptor, never()).sendAlert(any(), any());
        verify(response).sendRedirect("https://test.com/error");
    }

    @Test
    @DisplayName("알 수 없는 예외 발생 시 Slack에 알림 전송 후 UNKNOWN_ERROR로 Redirect한다.")
    void failure_unknown_error() throws Exception {

        // given
        OAuthParams params = mock(OAuthParams.class);
        AuthenticationException exception = mock(AuthenticationException.class);
        given(exception.getMessage()).willReturn("unknown");

        given(request.getParameter(OAuth2ParameterNames.STATE)).willReturn("valid");
        given(stateParser.getParams("valid")).willReturn(params);
        given(oauth2HandlerProperties.getErrorUrl(null, params)).willReturn("https://test.com/error");

        // when
        customFailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        verify(slackAdaptor).sendAlert(request, exception);
        verify(response).sendRedirect("https://test.com/error");
    }

    @Test
    @DisplayName("state 파싱 실패 시 Slack 알림 후 기본 페이지로 Redirect한다.")
    void failure_state_parsing_error() throws Exception {

        // given
        AuthenticationException exception = new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR);

        given(request.getParameter(OAuth2ParameterNames.STATE)).willReturn("invalid");
        given(stateParser.getParams("invalid")).willThrow(new OAuth2Exception(BbangleErrorCode.INVALID_OAUTH_PARAMS));
        given(oauth2HandlerProperties.getDefaultErrorUrl(BbangleErrorCode.INVALID_OAUTH_PARAMS))
            .willReturn("/oauth.html?error=INVALID_OAUTH_PARAMS");

        // when
        customFailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        verify(slackAdaptor).sendAlert(eq(request), any(OAuth2Exception.class));
        verify(response).sendRedirect("/oauth.html?error=INVALID_OAUTH_PARAMS");
    }
}