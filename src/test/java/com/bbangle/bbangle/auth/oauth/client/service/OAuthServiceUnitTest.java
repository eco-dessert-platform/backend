package com.bbangle.bbangle.auth.oauth.client.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위테스트] OAuthService")
@ExtendWith(MockitoExtension.class)
class OAuthServiceUnitTest {

    @InjectMocks
    private OAuthService service;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Nested
    @DisplayName("generateRefreshToken() Test")
    class GenerateRefreshTokenTest {

        @Test
        @DisplayName("Refresh Token을 생성하고 DB에 저장한다.")
        void success_create() {

            // given
            given(tokenProvider.generateToken(any(), any(), any())).willReturn("refreshToken");

            given(refreshTokenRepository.findByUserIdAndUserRole(1L, Role.ROLE_SELLER))
                .willReturn(Optional.empty());

            // when
            String token = service.generateRefreshToken(1L, Role.ROLE_SELLER);

            // then
            assertThat(token).isEqualTo("refreshToken");
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Refresh Token을 생성하고 DB에 업데이트한다.")
        void success_update() {

            // given
            RefreshToken existing = RefreshToken.create(1L, Role.ROLE_SELLER, "oldToken");

            given(tokenProvider.generateToken(any(), any(), any())).willReturn("newToken");

            given(refreshTokenRepository.findByUserIdAndUserRole(1L, Role.ROLE_SELLER))
                .willReturn(Optional.of(existing));

            // when
            String token = service.generateRefreshToken(1L, Role.ROLE_SELLER);

            // then
            assertThat(token).isEqualTo("newToken");
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("generateAccessToken() Test")
    class GenerateAccessTokenTest {

        @Test
        @DisplayName("Access Token을 생성한다.")
        void success() {

            // given
            given(tokenProvider.generateToken(any(), any(), any())).willReturn("accessToken");

            // when
            String token = service.generateAccessToken(1L, Role.ROLE_SELLER);

            // then
            assertThat(token).isEqualTo("accessToken");
        }
    }

    @Nested
    @DisplayName("refreshTokenValidate() Test")
    class RefreshTokenValidateTest {

        @Test
        @DisplayName("DB에 Refresh Token이 존재하면 통과한다.")
        void existToken() {

            // given
            String refreshToken = "validRefreshToken";
            RefreshToken existing = RefreshToken.create(1L, Role.ROLE_SELLER, "refreshToken");
            given(refreshTokenRepository.findByRefreshToken(any())).willReturn(Optional.of(existing));

            // when & then
            assertDoesNotThrow(() -> service.refreshTokenValidate(refreshToken));
        }

        @Test
        @DisplayName("DB에 Refresh Token이 없을 경우 UNAUTHORIZED 예외")
        void notExistToken() {

            // given
            String refreshToken = "invalidRefreshToken";
            given(refreshTokenRepository.findByRefreshToken(refreshToken)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.refreshTokenValidate(refreshToken))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode._UNAUTHORIZED);
                });
        }
    }

    @Nested
    @DisplayName("deleteRefreshToken() Test")
    class DeleteRefreshTokenTest {

        @Test
        @DisplayName("DB에 존재하는 Refresh Token을 삭제한다.")
        void deleteRefreshToken() {

            // given
            String refreshToken = "refreshToken";

            // when
            service.deleteRefreshToken(refreshToken);

            // then
            verify(refreshTokenRepository).deleteByRefreshToken(refreshToken);
        }
    }
}