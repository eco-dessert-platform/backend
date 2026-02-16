package com.bbangle.bbangle.config.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.bbangle.bbangle.auth.oauth.client.OAuth2StateParser;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO.UserType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.OAuth2Exception;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@DisplayName("[단위테스트] OAuth2ClientValidationFilter")
@ExtendWith(MockitoExtension.class)
class OAuth2ClientValidationFilterUnitTest {

    @InjectMocks
    private OAuth2ClientValidationFilter filter;

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @Mock
    private OAuth2HandlerProperties oAuth2HandlerProperties;

    @Mock
    private FilterChain filterChain;

    @Mock
    private OAuth2StateParser stateParser;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("등록되지 않은 OAuth2 Client 요청 시 프론트의 에러 페이지로 리다이렉트한다.")
    void redirect_client_not_found() throws Exception {

        // given
        request.setRequestURI("/oauth2/authorization/test");

        OAuth2HandlerProperties.RedirectUrl redirect = mock(OAuth2HandlerProperties.RedirectUrl.class);

        given(oAuth2HandlerProperties.redirect()).willReturn(redirect);
        given(redirect.error()).willReturn("/login");
        given(clientRegistrationRepository.findByRegistrationId("test")).willReturn(null);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl())
            .contains("/login")
            .contains("error=" + BbangleErrorCode.NOT_SUPPORTED_SERVER)
            .contains("code=" + BbangleErrorCode.NOT_SUPPORTED_SERVER.getCode());

        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("등록된 OAuth2 Client 요청 시 다음 필터로 진행한다.")
    void pass_client_exists() throws Exception {

        // given
        request.setRequestURI("/oauth2/authorization/test");
        request.setParameter("user", UserType.CUSTOMER.toString());

        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        OAuth2DTO.OAuthParams params = mock(OAuth2DTO.OAuthParams.class);

        given(clientRegistrationRepository.findByRegistrationId("test")).willReturn(clientRegistration);
        given(stateParser.getParams(anyString(), anyString())).willReturn(params);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(clientRegistrationRepository).findByRegistrationId("test");
        verify(filterChain).doFilter(any(HttpServletRequest.class), eq(response));
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    @DisplayName("OAuth2 로그인 URL에 profile 파라미터가 없을 경우 기본값으로 설정하여 다음 필터로 전달한다.")
    void oauth2_profileParam_default_wrapping() throws Exception {

        // given
        request.setRequestURI("/oauth2/authorization/test");
        request.setParameter("user", UserType.CUSTOMER.toString());

        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        OAuth2DTO.OAuthParams params = mock(OAuth2DTO.OAuthParams.class);

        given(clientRegistrationRepository.findByRegistrationId("test")).willReturn(clientRegistration);
        given(stateParser.getParams(anyString(), anyString())).willReturn(params);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        ArgumentCaptor<HttpServletRequest> captor = ArgumentCaptor.forClass(HttpServletRequest.class);

        verify(filterChain).doFilter(captor.capture(), eq(response));

        HttpServletRequest wrappedRequest = captor.getValue();
        assertThat(wrappedRequest.getParameter("profile")).isEqualTo(OAuth2DTO.defaultProfile(null));
        assertThat(response.getRedirectedUrl()).isNull();
    }

    @Test
    @DisplayName("OAuth2 파라미터가 유효하지 않으면 프론트의 에러 페이지로 리다이렉트한다.")
    void redirect_invalid_oauth_params() throws Exception {

        // given
        request.setRequestURI("/oauth2/authorization/test");
        request.setParameter("user", "INVALID"); // dto.valid() false 유도

        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        OAuth2HandlerProperties.RedirectUrl redirect = mock(OAuth2HandlerProperties.RedirectUrl.class);

        given(stateParser.getParams(any(), anyString())).willThrow(new OAuth2Exception(BbangleErrorCode.INVALID_OAUTH_PARAMS));
        given(oAuth2HandlerProperties.redirect()).willReturn(redirect);
        given(redirect.error()).willReturn("/login");
        given(clientRegistrationRepository.findByRegistrationId("test")).willReturn(clientRegistration);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);

        assertThat(response.getRedirectedUrl())
            .contains("/login")
            .contains("error=" + BbangleErrorCode.INVALID_OAUTH_PARAMS)
            .contains("code=" + BbangleErrorCode.INVALID_OAUTH_PARAMS.getCode());

        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("OAuth2 요청이 아니면 검증 없이 다음 필터로 진행한다.")
    void pass_non_oauth2_request() throws Exception {

        // given
        request.setRequestURI("/api/test");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(clientRegistrationRepository);
        assertThat(response.getRedirectedUrl()).isNull();
    }
}