package com.bbangle.bbangle.auth.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2DTO;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] OAuthSellerService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OAuthSellerServiceIntegrationTest {

    @Autowired
    EntityManager em;

    @Autowired
    private OAuthSellerService service;

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private TokenProvider tokenProvider;

    @Test
    @DisplayName("Redis에 저장된 SellerInfo를 조회하고 즉시 삭제한다.")
    void success_getSellerInfoFromRedis() {

        // given
        OAuth2DTO.InfoDTO sellerInfo = OAuth2DTO.InfoDTO.builder()
            .id(1L)
            .role(Role.ROLE_SELLER)
            .status(CertificationStatus.NEW)
            .build();

        redisRepository.setFromDTO(OAuthSellerService.OAUTH_CODE_NAMESPACE, "code", sellerInfo, Duration.ofMinutes(5));

        // when
        OAuth2DTO.InfoDTO result = service.getSellerInfoFromRedis("code");

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.role()).isEqualTo(Role.ROLE_SELLER);
        assertThat(result.status()).isEqualTo(CertificationStatus.NEW);

        assertThat(redisRepository.getDTOAndDelete(OAuthSellerService.OAUTH_CODE_NAMESPACE, "code", OAuth2DTO.InfoDTO.class))
            .isNull();
    }

    @Test
    @DisplayName("Redis에 SellerInfo가 없으면 UNAUTHORIZED 예외")
    void failure_getSellerInfoFromRedis() {

        // when & then
        assertThatThrownBy(() -> service.getSellerInfoFromRedis("code"))
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode._UNAUTHORIZED.getMessage());
    }

    @Test
    @DisplayName("Refresh Token을 생성하고 DB에 저장한다.")
    void generateRefreshToken_create() {

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
    void generateRefreshToken_update() {

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

    @Test
    @DisplayName("DB에 Refresh Token이 존재하면 통과한다.")
    void refreshTokenValidate_existToken() {

        // given
        String refreshToken = "validRefreshToken";
        RefreshToken existing = RefreshToken.create(1L, Role.ROLE_SELLER, refreshToken);
        refreshTokenRepository.save(existing);

        // when & then
        assertDoesNotThrow(() -> service.refreshTokenValidate(refreshToken));
    }

    @Test
    @DisplayName("DB에 Refresh Token이 없으면 UNAUTHORIZED 예외")
    void refreshTokenValidate_notExistToken() {

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