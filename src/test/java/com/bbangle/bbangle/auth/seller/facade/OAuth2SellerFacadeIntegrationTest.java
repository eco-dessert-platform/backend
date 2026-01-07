package com.bbangle.bbangle.auth.seller.facade;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.OAuth2SellerRepository;
import com.bbangle.bbangle.seller.seller.service.command.OAuth2ResponseCreateCommand;
import jakarta.persistence.EntityManager;
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
    EntityManager em;

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

        assertThat(savedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
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

        assertThat(result.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
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
}