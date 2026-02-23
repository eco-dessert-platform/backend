package com.bbangle.bbangle.auth.oauth.client.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.auth.oauth.client.dto.TokenClaimsDTO;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenResponse;
import com.bbangle.bbangle.auth.oauth.client.service.OAuthService;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위테스트] OAuthFacade")
@ExtendWith(MockitoExtension.class)
class OAuthFacadeUnitTest {

    @InjectMocks
    private OAuthFacade facade;

    @Mock
    private OAuthService service;

    @Mock
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("Refresh Token이 유효하면 토큰을 재발급한다.")
    void success_reissueToken() {

        // given
        String refreshToken = "validRefreshToken";
        TokenClaimsDTO tokenClaimsDTO = TokenClaimsDTO.builder()
            .id(1L)
            .role(Role.ROLE_SELLER)
            .build();

        given(tokenProvider.parseRefreshToken(refreshToken)).willReturn(tokenClaimsDTO);

        willDoNothing().given(service).refreshTokenValidate(refreshToken);
        willDoNothing().given(service).deleteRefreshToken(refreshToken);

        given(service.generateRefreshToken(1L, Role.ROLE_SELLER)).willReturn("newRefreshToken");
        given(service.generateAccessToken(1L, Role.ROLE_SELLER)).willReturn("newAccessToken");

        // when
        TokenResponse response = facade.reissueToken(refreshToken);

        // then
        assertThat(response.refreshToken()).isEqualTo("newRefreshToken");
        assertThat(response.accessToken()).isEqualTo("newAccessToken");

        InOrder inOrder = inOrder(tokenProvider, service);
        inOrder.verify(tokenProvider).parseRefreshToken(refreshToken);
        inOrder.verify(service).refreshTokenValidate(refreshToken);
        inOrder.verify(service).deleteRefreshToken(refreshToken);
        inOrder.verify(service).generateRefreshToken(1L, Role.ROLE_SELLER);
        inOrder.verify(service).generateAccessToken(1L, Role.ROLE_SELLER);
    }

    @Test
    @DisplayName("Refresh Token이 만료되면 예외 발생")
    void failure_reissueToken_expired() {

        // given
        String refreshToken = "expiredRefreshToken";

        given(tokenProvider.parseRefreshToken(refreshToken)).willThrow(new BbangleException(BbangleErrorCode._UNAUTHORIZED));

        // when & then
        assertThatThrownBy(() -> facade.reissueToken(refreshToken))
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> {
                BbangleException ex = (BbangleException) e;
                assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode._UNAUTHORIZED);
            });

        verify(service, never()).refreshTokenValidate(refreshToken);
        verify(service, never()).deleteRefreshToken(any());
        verify(service, never()).generateRefreshToken(any(), any());
        verify(service, never()).generateAccessToken(any(), any());
    }

    @Test
    @DisplayName("Refresh Token이 DB에 없으면 예외 발생")
    void failure_reissueToken_notExist() {

        // given
        String refreshToken = "invalidRefreshToken";
        TokenClaimsDTO tokenClaimsDTO = TokenClaimsDTO.builder()
            .id(1L)
            .role(Role.ROLE_SELLER)
            .build();

        given(tokenProvider.parseRefreshToken(refreshToken)).willReturn(tokenClaimsDTO);
        willThrow(new BbangleException(BbangleErrorCode._UNAUTHORIZED)).given(service).refreshTokenValidate(refreshToken);

        // when & then
        assertThatThrownBy(() -> facade.reissueToken(refreshToken))
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> {
                BbangleException ex = (BbangleException) e;
                assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode._UNAUTHORIZED);
            });

        verify(service, never()).deleteRefreshToken(any());
        verify(service, never()).generateRefreshToken(any(), any());
        verify(service, never()).generateAccessToken(any(), any());
    }
}