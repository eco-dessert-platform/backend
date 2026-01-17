package com.bbangle.bbangle.auth.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.auth.seller.service.dto.SellerInfoRedisDTO;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위테스트] OAuthSellerService")
@ExtendWith(MockitoExtension.class)
class OAuthSellerServiceUnitTest {

    @InjectMocks
    private OAuthSellerService service;

    @Mock
    private RedisRepository redisRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    // --------------------
    // getSellerInfoFromRedis Test
    // --------------------
    @Test
    @DisplayName("Redis에 저장된 SellerInfo를 조회하고 즉시 삭제한다.")
    void success_getSellerInfoFromRedis() {

        // given
        Map<Object, Object> sellerInfo = Map.of(
            "id", 1L,
            "role", Role.ROLE_SELLER.getRole(),
            "status", CertificationStatus.NEW.getDescription()
        );

        given(redisRepository.getMap(OAuthSellerService.OAUTH_CODE_NAMESPACE, "code")).willReturn(sellerInfo);

        // when
        SellerInfoRedisDTO result = service.getSellerInfoFromRedis("code");

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.role()).isEqualTo(Role.ROLE_SELLER);
        assertThat(result.status()).isEqualTo(CertificationStatus.NEW);

        verify(redisRepository).delete(OAuthSellerService.OAUTH_CODE_NAMESPACE, "code");
    }

    @Test
    @DisplayName("Redis로부터 Seller의 정보 조회를 실패한다.")
    void failure_getSellerInfoFromRedis() {

        // given
        given(redisRepository.getMap(OAuthSellerService.OAUTH_CODE_NAMESPACE, "code")).willReturn(Map.of());

        // when & then
        assertThatThrownBy(() -> service.getSellerInfoFromRedis("code"))
            .isInstanceOf(BbangleException.class);
    }

    // --------------------
    // generateRefreshToken Test
    // --------------------
    @Test
    @DisplayName("Refresh Token을 생성하고 DB에 저장한다.")
    void success_generateRefreshToken_create() {

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
    void success_generateRefreshToken_update() {

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

    // --------------------
    // generateRefreshToken Test
    // --------------------
    @Test
    @DisplayName("Access Token을 생성한다.")
    void success_generateAccessToken() {

        // given
        given(tokenProvider.generateToken(any(), any(), any())).willReturn("accessToken");

        // when
        String token = service.generateAccessToken(1L, Role.ROLE_SELLER);

        // then
        assertThat(token).isEqualTo("accessToken");
    }
}