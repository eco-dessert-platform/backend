package com.bbangle.bbangle.config.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("등록되지 않은 OAuth2 Client 요청 시 로그인 페이지로 리다이렉트한다.")
    void redirect_client_not_found() throws Exception {

        // given
        request.setRequestURI("/oauth2/authorization/test");

        given(oAuth2HandlerProperties.getError()).willReturn("/login");
        given(clientRegistrationRepository.findByRegistrationId("test")).willReturn(null);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo(
            "/login?error=" + BbangleErrorCode.NOT_SUPPORTED_SERVER +
                "&code=" + BbangleErrorCode.NOT_SUPPORTED_SERVER.getCode()
        );

        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    @DisplayName("등록된 OAuth2 Client 요청 시 다음 필터로 진행한다.")
    void pass_client_exists() throws Exception {

        // given
        request.setRequestURI("/oauth2/authorization/test");

        ClientRegistration clientRegistration = mock(ClientRegistration.class);
        given(clientRegistrationRepository.findByRegistrationId("test")).willReturn(clientRegistration);

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(clientRegistrationRepository).findByRegistrationId("test");
        verify(filterChain).doFilter(request, response);
        assertThat(response.getRedirectedUrl()).isNull();
    }
}