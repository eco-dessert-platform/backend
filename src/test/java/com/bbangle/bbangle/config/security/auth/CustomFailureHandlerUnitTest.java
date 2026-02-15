package com.bbangle.bbangle.config.security.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2Redis.OAuthParams;
import com.bbangle.bbangle.common.adaptor.slack.SlackAdaptor;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

@DisplayName("[단위테스트] CustomFailureHandler")
@ExtendWith(MockitoExtension.class)
class CustomFailureHandlerUnitTest {

    private final String STATE = "test-state";

    @Mock
    SlackAdaptor slackAdaptor;

    @Mock
    OAuth2HandlerProperties oAuth2HandlerProperties;

    @Mock
    RedisRepository redisRepository;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    CustomFailureHandler customFailureHandler;

    @BeforeEach
    void setUp() {
        customFailureHandler = new CustomFailureHandler(slackAdaptor, oAuth2HandlerProperties, redisRepository);
        given(request.getParameter("state")).willReturn(STATE);
    }

    @Nested
    @DisplayName("유효한 OAuth2 Params일 경우")
    class Valid_Params {

        OAuthParams params;

        @BeforeEach
        void setUp() {
            params = mock(OAuthParams.class);
            given(redisRepository.getDTOAndDelete(
                eq(CustomFailureHandler.OAUTH_STATE_NAMESPACE),
                eq(STATE),
                eq(OAuthParams.class)
            )).willReturn(params);
        }

        @Test
        @DisplayName("5xx 에러 발생 시 Slack 알림 후 에러 URL로 Redirect한다.")
        void failure_5xx_error() throws Exception {

            // given
            OAuth2Exception exception = new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR);

            given(oAuth2HandlerProperties.getErrorUrl(params, BbangleErrorCode.INTERNAL_SERVER_ERROR))
                .willReturn("https://error-url");

            // when
            customFailureHandler.onAuthenticationFailure(request, response, exception);

            // then
            verify(slackAdaptor).sendAlert(request, exception);
            verify(response).sendRedirect("https://error-url");
        }

        @Test
        @DisplayName("4xx 에러 발생 시 Slack 없이 Redirect")
        void failure_4xx_error() throws Exception {

            // given
            OAuth2Exception exception = new OAuth2Exception(BbangleErrorCode.NOT_SUPPORTED_SERVER);

            given(oAuth2HandlerProperties.getErrorUrl(params, BbangleErrorCode.NOT_SUPPORTED_SERVER))
                .willReturn("https://error-url");

            // when
            customFailureHandler.onAuthenticationFailure(request, response, exception);

            // then
            verify(slackAdaptor, never()).sendAlert(any(), any());
            verify(response).sendRedirect("https://error-url");
        }

        @Test
        @DisplayName("알 수 없는 예외 발생 시 Slack에 알림 전송 후 UNKNOWN_ERROR로 Redirect한다.")
        void failure_unknown_error() throws Exception {

            // given
            AuthenticationException exception = new AuthenticationException("Unknown") {};

            given(oAuth2HandlerProperties.getErrorUrl(params, null)).willReturn("https://unknown-error");

            // when
            customFailureHandler.onAuthenticationFailure(request, response, exception);

            // then
            verify(slackAdaptor).sendAlert(request, exception);
            verify(response).sendRedirect("https://unknown-error");
        }

        @Test
        @DisplayName("OAUTH_INVALID_PARAMS면 HTML 파일로 Redirect 한다.")
        void failure_invalid_params_code() throws Exception {

            // given
            OAuth2Exception exception = new OAuth2Exception(BbangleErrorCode.OAUTH_INVALID_PARAMS);

            // when
            customFailureHandler.onAuthenticationFailure(request, response, exception);

            // then
            verify(response).sendRedirect("/oauth.html?error=" + BbangleErrorCode.OAUTH_INVALID_PARAMS.name());
            verify(slackAdaptor, never()).sendAlert(any(), any());
        }
    }

    @Nested
    @DisplayName("OAuth Params가 잘못됬거나 가져올 수 없는 경우")
    class Invalid_Params {

        @Test
        @DisplayName("state가 없으면 INVALID_PARAMS로 Redirect한다.")
        void failure_state_missing() throws Exception {

            // given
            given(request.getParameter("state")).willReturn(null);

            OAuth2Exception exception = new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR);

            // when
            customFailureHandler.onAuthenticationFailure(request, response, exception);

            // then
            verify(response).sendRedirect("/oauth.html?error=" + BbangleErrorCode.OAUTH_INVALID_PARAMS.name());
            verify(slackAdaptor, never()).sendAlert(any(), any());
        }

        @Test
        @DisplayName("Redis에서 OAuthParams가 null이면 INVALID_PARAMS로 Redirect")
        void failure_oauth_params_null() throws Exception {

            // given
            given(redisRepository.getDTOAndDelete(
                eq(CustomFailureHandler.OAUTH_STATE_NAMESPACE),
                eq(STATE),
                eq(OAuthParams.class)
            )).willReturn(null);

            OAuth2Exception exception = new OAuth2Exception(BbangleErrorCode.INTERNAL_SERVER_ERROR);

            // when
            customFailureHandler.onAuthenticationFailure(request, response, exception);

            // then
            verify(response).sendRedirect("/oauth.html?error=" + BbangleErrorCode.OAUTH_INVALID_PARAMS.name());
            verify(slackAdaptor, never()).sendAlert(any(), any());
        }
    }
}