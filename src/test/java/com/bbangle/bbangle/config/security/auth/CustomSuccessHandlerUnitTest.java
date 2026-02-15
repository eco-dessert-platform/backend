package com.bbangle.bbangle.config.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2Redis;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2Redis.InfoDTO;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
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
        OAuth2Redis.OAuthParams params = mock(OAuth2Redis.OAuthParams.class);

        CustomUserDetails userDetails = CustomUserDetails.builder()
            .id(1L)
            .role(Role.ROLE_SELLER)
            .name("test")
            .status(CertificationStatus.NEW)
            .params(params)   // 반드시 추가
            .build();

        given(authentication.getPrincipal()).willReturn(userDetails);

        ArgumentCaptor<String> uuidCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InfoDTO> dtoCaptor = ArgumentCaptor.forClass(InfoDTO.class);

        given(oauth2HandlerProperties.getSuccessUrl(eq(params), any(UUID.class)))
            .willReturn("https://success-url");

        // when
        customSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        verify(redisRepository).setFromDTO(
            eq(CustomSuccessHandler.OAUTH_CODE_NAMESPACE),
            uuidCaptor.capture(),
            dtoCaptor.capture(),
            eq(CustomSuccessHandler.TEMP_CODE_TTL)
        );

        InfoDTO value = dtoCaptor.getValue();
        assertThat(value.id()).isEqualTo(1L);
        assertThat(value.role()).isEqualTo(Role.ROLE_SELLER);
        assertThat(value.status()).isEqualTo(CertificationStatus.NEW);

        verify(response).sendRedirect("https://success-url");
    }

    @Test
    @DisplayName("Redis 저장 중 예외 발생 시 OAuth2Exception을 던진다.")
    void fail_onAuthenticationSuccess_redis_error() throws Exception {

        // given
        OAuth2Redis.OAuthParams params = mock(OAuth2Redis.OAuthParams.class);

        CustomUserDetails userDetails = CustomUserDetails.builder()
            .id(1L)
            .role(Role.ROLE_SELLER)
            .name("test")
            .status(CertificationStatus.NEW)
            .params(params)
            .build();

        given(authentication.getPrincipal()).willReturn(userDetails);

        RuntimeException originalEx = new RuntimeException("Redis Down");

        doThrow(originalEx)
            .when(redisRepository)
            .setFromDTO(any(), any(), any(), any());

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