package com.bbangle.bbangle.config.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

@DisplayName("[단위테스트] CustomSuccessHandler")
@ExtendWith(MockitoExtension.class)
class CustomSuccessHandlerUnitTest {

    CustomSuccessHandler customSuccessHandler;

    @Mock
    TokenProvider tokenProvider;

    @Mock
    RedisRepository redisRepository;

    @Mock
    Authentication authentication;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Test
    @DisplayName("OAuth2 로그인 성공 시 RefreshToken을  Redis에 저장하고 인가 코드를 담아서 Redirect한다.")
    void success_onAuthenticationSuccess() throws Exception {

        // given
        OAuth2HandlerProperties properties = new OAuth2HandlerProperties();
        properties.setSuccess("https://test.com/login");

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(1L)
                .role(Role.ROLE_SELLER)
                .name("test")
                .build();

        given(authentication.getPrincipal()).willReturn(userDetails);
        given(tokenProvider.generateToken(
                userDetails.id(),
                userDetails.role(),
                CustomSuccessHandler.REFRESH_TOKEN_DURATION
        )).willReturn("refreshToken");

        customSuccessHandler = new CustomSuccessHandler(properties, tokenProvider, redisRepository);

        // when
        customSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        ArgumentCaptor<String> uuidCaptor = ArgumentCaptor.forClass(String.class);   //  UUID 값 저장
        verify(redisRepository).setFromString(
                eq("oauth2:code"),
                uuidCaptor.capture(),
                eq("refreshToken"),
                eq(CustomSuccessHandler.REFRESH_TOKEN_TTL)
        );

        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertThat(redirectUrl).isEqualTo(
                properties.getSuccess() + "?generateToken=" + uuidCaptor.getValue()
        );
    }

    @Test
    @DisplayName("Redis 저장 중 예외 발생 시 OAuth2Exception을 던진다.")
    void fail_onAuthenticationFailure_redis_error() throws IOException {

        // given
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(1L)
                .role(Role.ROLE_SELLER)
                .name("test")
                .build();

        RuntimeException originalEx = new RuntimeException("Redis Down");

        OAuth2HandlerProperties properties = new OAuth2HandlerProperties();
        customSuccessHandler = new CustomSuccessHandler(properties, tokenProvider, redisRepository);

        given(authentication.getPrincipal()).willReturn(userDetails);
        given(tokenProvider.generateToken(any(), any(), any())).willReturn("refreshToken");

        doThrow(originalEx)
                .when(redisRepository)
                .setFromString(any(), any(), any(), any());

        // when & then
        assertThatThrownBy(() ->
                customSuccessHandler.onAuthenticationSuccess(request, response, authentication)
        )
                .isInstanceOf(OAuth2Exception.class)
                .hasCause(originalEx)
                .satisfies(ex -> {
                    OAuth2Exception oAuth2Ex = (OAuth2Exception) ex;
                    assertThat(oAuth2Ex.getCode())
                            .isEqualTo(BbangleErrorCode.INTERNAL_SERVER_ERROR);
                });

        verify(response, never()).sendRedirect(any());
    }
}