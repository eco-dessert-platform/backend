package com.bbangle.bbangle.config.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.auth.oauth.client.OAuth2StateParser;
import com.bbangle.bbangle.auth.oauth.client.dto.CustomUserDetails;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.OAuthParams;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    @Mock
    OAuth2StateParser stateParser;

    @BeforeEach
    void setUp() {
        customSuccessHandler = new CustomSuccessHandler(oauth2HandlerProperties, redisRepository, stateParser);
    }

    @Test
    @DisplayName("OAuth2 로그인 성공 시 Redis에 UserInfo를 저장하고 인가 코드를 담아서 Redirect한다.")
    void success_onAuthenticationSuccess() throws Exception {

        // given
        CustomUserDetails userDetails = CustomUserDetails.builder()
            .id(1L)
            .role(Role.ROLE_SELLER)
            .name("test")
            .status(CertificationStatus.NEW)
            .build();

        given(authentication.getPrincipal()).willReturn(userDetails);

        // state mock
        String fakeState = "encoded-state";
        given(request.getParameter("state")).willReturn(fakeState);

        // Parser static 메서드 mocking 필요
        OAuthParams params = mock(OAuthParams.class);
        given(stateParser.getParams(eq(fakeState))).willReturn(params);

        // redirect URL mock
        given(oauth2HandlerProperties.getSuccessUrl(any(), eq(params)))
            .willAnswer(invocation -> {
                UUID uuid = invocation.getArgument(0);
                return "https://test.com/success?generateToken=" + uuid;
            });
        given(response.encodeRedirectURL(anyString())).willAnswer(invocation -> invocation.getArgument(0));

        // when
        customSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        ArgumentCaptor<String> uuidCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<OAuth2DTO.InfoDTO> dtoCaptor = ArgumentCaptor.forClass(OAuth2DTO.InfoDTO.class);

        verify(redisRepository).setFromDTO(
            eq(CustomSuccessHandler.OAUTH_CODE_NAMESPACE),
            uuidCaptor.capture(),
            dtoCaptor.capture(),
            eq(CustomSuccessHandler.TEMP_CODE_TTL)
        );

        OAuth2DTO.InfoDTO value = dtoCaptor.getValue();
        assertThat(value.id()).isEqualTo(1L);
        assertThat(value.role()).isEqualTo(Role.ROLE_SELLER);
        assertThat(value.status()).isEqualTo(CertificationStatus.NEW);

        verify(response).sendRedirect("https://test.com/success?generateToken=" + uuidCaptor.getValue());
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
        given(request.getParameter("state")).willReturn("state");

        OAuthParams params = mock(OAuthParams.class);
        given(stateParser.getParams(any())).willReturn(params);

        RuntimeException originalEx = new RuntimeException("Redis Down");
        doThrow(originalEx).when(redisRepository).setFromDTO(any(), any(), any(), any());

        // when & then
        assertThatThrownBy(() -> customSuccessHandler
            .onAuthenticationSuccess(request, response, authentication))
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