package com.bbangle.bbangle.auth.oauth.client.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenResponse;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] OAuthFacade")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OAuthFacadeIntegrationTest {

    @Autowired
    OAuthFacade facade;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    TokenProvider tokenProvider;

    @Test
    @DisplayName("Refresh Token이 유효하면 토큰을 재발급한다.")
    void success_reissueToken() {

        // given
        Long sellerId = 1L;
        Role role = Role.ROLE_SELLER;
        String refreshToken = tokenProvider.generateToken(sellerId, role, Duration.ofDays(14));

        RefreshToken entity = RefreshToken.create(sellerId, role, refreshToken);
        RefreshToken oldToken = refreshTokenRepository.saveAndFlush(entity);

        // when
        TokenResponse result = facade.reissueToken(refreshToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isNotNull();
        assertThat(result.refreshToken()).isNotNull();

        RefreshToken updated = refreshTokenRepository.findByUserIdAndUserRole(sellerId, role).orElseThrow();
        assertThat(oldToken.getId()).isNotEqualTo(updated.getId());
    }

    @Test
    @DisplayName("Refresh Token이 DB에 없으면 예외 발생")
    void failure_reissueToken_notExist() {

        // given
        Long sellerId = 1L;
        Role role = Role.ROLE_SELLER;
        String refreshToken = tokenProvider.generateToken(sellerId, role, Duration.ofDays(14));

        // when & then
        assertThatThrownBy(() -> facade.reissueToken(refreshToken))
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> {
                BbangleException ex = (BbangleException) e;
                assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode._UNAUTHORIZED);
            });
    }

}