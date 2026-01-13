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
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
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
    RedisRepository redisRepository;

    @Mock
    Authentication authentication;

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    OAuth2HandlerProperties oauth2HandlerProperties;

    @BeforeEach
    void setUp() {
        customSuccessHandler = new CustomSuccessHandler(oauth2HandlerProperties, redisRepository);
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 시 RefreshToken을  Redis에 저장하고 인가 코드를 담아서 Redirect한다.")
    void success_onAuthenticationSuccess() throws Exception {

        // given
        CustomUserDetails userDetails = CustomUserDetails.builder()
            .id(1L)
            .role(Role.ROLE_SELLER)
            .name("test")
            .status(CertificationStatus.NEW)
            .build();

        given(authentication.getPrincipal()).willReturn(userDetails);
        given(oauth2HandlerProperties.success()).willReturn("https://test.com/success");

        // when
        customSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        ArgumentCaptor<String> uuidCaptor = ArgumentCaptor.forClass(String.class);  //  UUID 값 저장
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);

        verify(redisRepository).setFromMap(
                eq("oauth2:code"),
                uuidCaptor.capture(),
                mapCaptor.capture(),
                eq(CustomSuccessHandler.TEMP_CODE_TTL)
        );

        // Map 검증
        Map<String, Object> value = mapCaptor.getValue();
        assertThat(value)
            .containsEntry("id", 1L)
            .containsEntry("Role", Role.ROLE_SELLER)
            .containsEntry("Status", CertificationStatus.NEW);

        // redirect 검증
        ArgumentCaptor<String> redirectCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(redirectCaptor.capture());

        String redirectUrl = redirectCaptor.getValue();
        assertThat(redirectUrl).isEqualTo(
            "https://test.com/success?generateToken=" + uuidCaptor.getValue()
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
            .status(CertificationStatus.NEW)
            .build();

        given(authentication.getPrincipal()).willReturn(userDetails);

        RuntimeException originalEx = new RuntimeException("Redis Down");
        doThrow(originalEx)
            .when(redisRepository)
            .setFromMap(any(), any(), any(), any());

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