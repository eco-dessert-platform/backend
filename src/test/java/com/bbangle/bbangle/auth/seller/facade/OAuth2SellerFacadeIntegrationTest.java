package com.bbangle.bbangle.auth.seller.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.auth.domain.RefreshToken;
import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.auth.oauth.client.dto.OAuth2InfoRedisDTO;
import com.bbangle.bbangle.auth.oauth.client.dto.TokenResponse;
import com.bbangle.bbangle.auth.seller.controller.dto.GenerateTokenResponse;
import com.bbangle.bbangle.auth.seller.service.OAuthSellerService;
import com.bbangle.bbangle.common.redis.repository.RedisRepository;
import com.bbangle.bbangle.common.redis.repository.RefreshTokenRepository;
import com.bbangle.bbangle.common.role.Role;
import com.bbangle.bbangle.config.security.jwt.TokenProvider;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.OAuth2SellerRepository;
import com.bbangle.bbangle.seller.seller.service.command.OAuth2ResponseCreateCommand;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] OAuth2SellerFacade")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OAuth2SellerFacadeIntegrationTest {

    @Autowired
    OAuth2SellerFacade oAuth2SellerFacade;

    @Autowired
    OAuth2SellerRepository oAuth2SellerRepository;

    @Autowired
    RedisRepository redisRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    EntityManager em;

    @Autowired
    TokenProvider tokenProvider;

    @Test
    @DisplayName("판매자 계정이 없으면 새로 생성한다.")
    void success_login_seller_create() {

        // given
        OAuth2ResponseCreateCommand command =
                OAuth2ResponseCreateCommand.builder()
                        .name("test")
                        .nickname("test")
                        .provider(OauthServerType.KAKAO)
                        .providerId("12345")
                        .build();

        // when
        oAuth2SellerFacade.login(command);
        em.flush();
        em.clear();

        // then
        OAuth2Seller savedSeller = oAuth2SellerRepository.findByProviderAndProviderId(
                command.provider(), command.providerId()
        ).orElseThrow();

        assertThat(savedSeller).isNotNull();
        assertThat(savedSeller.getName()).isEqualTo(command.name());
        assertThat(savedSeller.getProvider()).isEqualTo(command.provider());
        assertThat(savedSeller.getProviderId()).isEqualTo(command.providerId());

        assertThat(savedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.NEW);
        assertThat(savedSeller.isDeleted()).isFalse();
        assertThat(savedSeller.getStore()).isNull();
    }

    @Test
    @DisplayName("판매자 계정이 존재하면 생성하지 않고 반환한다.")
    void success_login_seller_exist() {

        // given
        OAuth2ResponseCreateCommand command =
                OAuth2ResponseCreateCommand.builder()
                        .name("test")
                        .nickname("test")
                        .provider(OauthServerType.KAKAO)
                        .providerId("12345")
                        .build();

        OAuth2Seller existingSeller = oAuth2SellerRepository.saveAndFlush(
                OAuth2Seller.create(
                        command.resolvedName(),
                        command.provider(),
                        command.providerId()
                )
        );

        // when
        OAuth2Seller result = oAuth2SellerFacade.login(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingSeller.getId());
        assertThat(result.getName()).isEqualTo(existingSeller.getName());
        assertThat(result.getProvider()).isEqualTo(existingSeller.getProvider());
        assertThat(result.getProviderId()).isEqualTo(existingSeller.getProviderId());

        assertThat(result.getCertificationStatus()).isEqualTo(CertificationStatus.NEW);
        assertThat(result.isDeleted()).isFalse();
        assertThat(result.getStore()).isNull();
    }

    @Test
    @DisplayName("동시에 OAuth2 로그인을 시도할 경우 UNIQUE 충돌 시 재조회 후 반환한다.")
    void login_unique_constraint_violation() throws Exception {

        // given
        OAuth2ResponseCreateCommand command =
                OAuth2ResponseCreateCommand.builder()
                        .name("test")
                        .nickname("test")
                        .provider(OauthServerType.KAKAO)
                        .providerId("12345")
                        .build();

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<Future<OAuth2Seller>> futures = new ArrayList<>();

        // when
        for (int i = 0; i < threadCount; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    return oAuth2SellerFacade.login(command);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await();  // 모든 스레드 종료 대기
        executorService.shutdown();

        // then
        assertThat(oAuth2SellerRepository.count()).isEqualTo(1);    // DB에는 판매자 게정이 1개만 존재해야한다.

        // 모든 스레드가 동일한 판매자 계정을 반환해야한다.
        Set<Long> sellerIds = new HashSet<>();
        for (Future<OAuth2Seller> future : futures) {
            sellerIds.add(future.get().getId());
        }

        assertThat(sellerIds).hasSize(1);
    }

    @Test
    @DisplayName("임시 코드로 토큰을 발급하고 RefreshToken을 DB에 저장한다.")
    void success_generateToken() {

        // given
        String code = "oAuthCode";
        OAuth2InfoRedisDTO sellerInfo = OAuth2InfoRedisDTO.builder()
            .id(1L)
            .role(Role.ROLE_SELLER)
            .status(CertificationStatus.NEW)
            .build();

        redisRepository.setFromDTO(OAuthSellerService.OAUTH_CODE_NAMESPACE, code, sellerInfo, Duration.ofMinutes(5));

        // when
        GenerateTokenResponse response = oAuth2SellerFacade.generateToken(code);

        // then
        assertThat(response.sellerId()).isEqualTo(1L);
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.status()).isEqualTo(CertificationStatus.NEW);

        assertThat(redisRepository.getDTOAndDelete(OAuthSellerService.OAUTH_CODE_NAMESPACE, code, OAuth2InfoRedisDTO.class))
            .isNull();

        RefreshToken saved = refreshTokenRepository
            .findByUserIdAndUserRole(1L, Role.ROLE_SELLER)
            .orElseThrow();

        assertThat(saved.getRefreshToken()).isEqualTo(response.refreshToken());
    }

    @Test
    @DisplayName("Redis에 임시 코드가 없으면 UNAUTHORIZED 예외를 던진다.")
    void failure_generateToken_code_notFound() {

        // given
        String code = "invalidCode";

        // when & then
        assertThatThrownBy(() -> oAuth2SellerFacade.generateToken(code))
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> {
                BbangleException ex = (BbangleException) e;
                assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode._UNAUTHORIZED);
            });
    }

    @Test
    @DisplayName("Refresh Token이 유효하면 토큰을 재발급한다.")
    void success_reissueToken() {

        // given
        Long sellerId = 1L;
        Role role = Role.ROLE_SELLER;
        String refreshToken = tokenProvider.generateToken(sellerId, role, Duration.ofDays(14));

        RefreshToken entity = RefreshToken.create(sellerId, role, refreshToken);
        refreshTokenRepository.save(entity);

        // when
        TokenResponse result = oAuth2SellerFacade.reissueToken(refreshToken);

        // then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isNotNull();
        assertThat(result.refreshToken()).isNotNull();

        RefreshToken updated = refreshTokenRepository.findByUserIdAndUserRole(sellerId, role).orElseThrow();
        assertThat(result.refreshToken()).isEqualTo(updated.getRefreshToken());
    }

    @Test
    @DisplayName("Refresh Token이 유효하면 토큰을 재발급한다.")
    void failure_reissueToken_expired() {

        // given
        Long sellerId = 1L;
        Role role = Role.ROLE_SELLER;
        String refreshToken = tokenProvider.generateToken(sellerId, role, Duration.ofMillis(1));

        RefreshToken entity = RefreshToken.create(sellerId, role, refreshToken);
        refreshTokenRepository.save(entity);

        try { Thread.sleep(3); } catch (InterruptedException e) {}

        // when & then
        assertThatThrownBy(() -> oAuth2SellerFacade.reissueToken(refreshToken))
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> {
                BbangleException ex = (BbangleException) e;
                assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode._UNAUTHORIZED);
            });
    }

    @Test
    @DisplayName("Refresh Token이 DB에 없으면 예외 발생")
    void failure_reissueToken_notExist() {

        // given
        Long sellerId = 1L;
        Role role = Role.ROLE_SELLER;
        String refreshToken = tokenProvider.generateToken(sellerId, role, Duration.ofDays(14));

        // when & then
        assertThatThrownBy(() -> oAuth2SellerFacade.reissueToken(refreshToken))
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> {
                BbangleException ex = (BbangleException) e;
                assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode._UNAUTHORIZED);
            });
    }
}