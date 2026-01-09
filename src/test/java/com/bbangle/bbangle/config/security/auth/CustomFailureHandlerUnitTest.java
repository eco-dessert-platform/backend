package com.bbangle.bbangle.config.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

@DisplayName("[단위테스트] CustomFailureHandler")
@ExtendWith(MockitoExtension.class)
class CustomFailureHandlerUnitTest {

    @Mock
    SlackAdaptor slackAdaptor;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    CustomFailureHandler customFailureHandler;

    @Test
    @DisplayName("5xx 에러 발생 시 Slack에 알림 전송 후 Redirect한다.")
    void failure_5xx_error() throws Exception {

        // given
        OAuth2Exception exception = new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR);
        OAuth2HandlerProperties properties = new OAuth2HandlerProperties();
        properties.setError("https://test.com/login");

        customFailureHandler = new CustomFailureHandler(slackAdaptor, properties);

        // when
        customFailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        verify(slackAdaptor).sendAlert(eq(request), eq(exception));

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertThat(redirectUrl).isEqualTo(
                properties.getError() +
                        "?error=" + BbangleErrorCode.INTERNAL_SERVER_ERROR +
                        "&code=" + BbangleErrorCode.INTERNAL_SERVER_ERROR.getCode()
        );
    }

    @Test
    @DisplayName("4xx 에러 발생 시 Slack에 알림 없이 Redirect한다.")
    void failure_4xx_error() throws Exception {

        // given
        OAuth2Exception exception = new OAuth2Exception(BbangleErrorCode.NOT_SUPPORTED_SERVER);
        OAuth2HandlerProperties properties = new OAuth2HandlerProperties();
        properties.setError("https://test.com/login");

        customFailureHandler = new CustomFailureHandler(slackAdaptor, properties);

        // when
        customFailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        verify(slackAdaptor, never()).sendAlert(any(), any());

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertThat(redirectUrl).isEqualTo(
                properties.getError() +
                        "?error=" + BbangleErrorCode.NOT_SUPPORTED_SERVER +
                        "&code=" + BbangleErrorCode.NOT_SUPPORTED_SERVER.getCode()
        );
    }

    @Test
    @DisplayName("알 수 없는 예외 발생 시 UNKNOWN_ERROR로 Redirect한다.")
    void failure_unknown_error() throws Exception {

        // given
        AuthenticationException exception = new AuthenticationException("Unknown error") {};
        OAuth2HandlerProperties properties = new OAuth2HandlerProperties();
        properties.setError("https://test.com/login");

        customFailureHandler = new CustomFailureHandler(slackAdaptor, properties);

        // when
        customFailureHandler.onAuthenticationFailure(request, response, exception);

        // then
        verify(slackAdaptor, never()).sendAlert(any(), any());

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertThat(redirectUrl).isEqualTo(
                properties.getError() + "?error=" + "UNKNOWN_ERROR"
        );
    }
}