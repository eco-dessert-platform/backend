package com.bbangle.bbangle.auth.oauth.client.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] OAuthService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OAuthServiceIntegrationTest {

    @Autowired
    EntityManager em;

    @Autowired
    private OAuthService service;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private TokenProvider tokenProvider;

    @Nested
    @DisplayName("generateRefreshToken() Test")
    class GenerateRefreshTokenTest {

        @Test
        @DisplayName("Refresh Token을 생성하고 DB에 저장한다.")
        void create() {

            // given
            given(tokenProvider.generateToken(any(), any(), any())).willReturn("refreshToken");

            // when
            String token = service.generateRefreshToken(1L, Role.ROLE_SELLER);
            em.flush();
            em.clear();

            // then
            assertThat(token).isEqualTo("refreshToken");

            RefreshToken saved = refreshTokenRepository
                .findByUserIdAndUserRole(1L, Role.ROLE_SELLER)
                .orElseThrow();

            assertThat(saved.getRefreshToken()).isEqualTo("refreshToken");
        }

        @Test
        @DisplayName("Refresh Token을 생성하고 DB에 업데이트한다.")
        void update() {

            // given
            RefreshToken existing = RefreshToken.create(
                1L,
                Role.ROLE_SELLER,
                "oldToken"
            );
            refreshTokenRepository.save(existing);

            given(tokenProvider.generateToken(any(), any(), any())).willReturn("newToken");

            // when
            service.generateRefreshToken(1L, Role.ROLE_SELLER);
            em.flush();
            em.clear();

            // then
            RefreshToken updated = refreshTokenRepository
                .findByUserIdAndUserRole(1L, Role.ROLE_SELLER)
                .orElseThrow();

            assertThat(updated.getRefreshToken()).isEqualTo("newToken");
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
            RefreshToken existing = RefreshToken.create(1L, Role.ROLE_SELLER, refreshToken);
            refreshTokenRepository.save(existing);

            // when & then
            assertDoesNotThrow(() -> service.refreshTokenValidate(refreshToken));
        }

        @Test
        @DisplayName("DB에 Refresh Token이 없으면 UNAUTHORIZED 예외")
        void notExistToken() {

            // given
            String refreshToken = "invalidRefreshToken";

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
        @DisplayName("로그아웃 시 DB의 Refresh Token을 삭제한다.")
        void deleteRefreshToken() {

            // given
            String refreshToken = "refreshToken";
            RefreshToken exist = RefreshToken.create(
                1L,
                Role.ROLE_SELLER,
                refreshToken
            );
            refreshTokenRepository.save(exist);

            // when
            service.deleteRefreshToken(refreshToken);

            // then
            assertThat(refreshTokenRepository.findByUserIdAndUserRole(1L, Role.ROLE_SELLER)).isEmpty();
            assertThat(refreshTokenRepository.findByRefreshToken(refreshToken)).isEmpty();
        }
    }
}