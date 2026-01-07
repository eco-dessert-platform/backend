package com.bbangle.bbangle.seller.seller.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.seller.domain.OAuth2Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.OAuth2SellerRepository;
import com.bbangle.bbangle.seller.seller.service.command.OAuth2ResponseCreateCommand;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[통합테스트] OAuth2SellerServiceIntegrationTest")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OAuth2SellerServiceIntegrationTest {

    @Autowired
    OAuth2SellerService oAuth2SellerService;

    @Autowired
    OAuth2SellerRepository oAuth2SellerRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("provider + providerId로 판매자 계정을 조회한다.")
    void success_findByProviderAndProviderId() {

        // given
        OAuth2Seller seller = OAuth2Seller.create(
                "test",
                OauthServerType.KAKAO,
                "12345"
        );
        oAuth2SellerRepository.save(seller);
        em.flush();
        em.clear();

        // when
        Optional<OAuth2Seller> result = oAuth2SellerService.findByProviderAndProviderId(OauthServerType.KAKAO, "12345");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(seller.getName());
        assertThat(result.get().getProvider()).isEqualTo(seller.getProvider());
        assertThat(result.get().getProviderId()).isEqualTo(seller.getProviderId());
    }

    @Test
    @DisplayName("provider + providerId로 존재하지 않는 판매자 계정을 조회한다.")
    void success_findByProviderAndProviderId_empty() {

        // when
        Optional<OAuth2Seller> result = oAuth2SellerService.findByProviderAndProviderId(OauthServerType.KAKAO, "NOT_EXIST");

        // then
        assertThat(result).isNotPresent();
    }

    @Test
    @DisplayName("Name이 있을 경우 Name을 사용해 OAuth2 판매자를 생성한다.")
    void success_create_OAuth2Seller_withName() {

        // given
        OAuth2ResponseCreateCommand command = OAuth2ResponseCreateCommand.builder()
                .name("test")
                .nickname(null)
                .provider(OauthServerType.KAKAO)
                .providerId("12345")
                .build();

        // when
        oAuth2SellerService.createOAuth2Seller(command);
        em.flush();
        em.clear();

        // then
        OAuth2Seller savedSeller = oAuth2SellerRepository.findAll().get(0);

        assertThat(savedSeller).isNotNull();
        assertThat(savedSeller.getName()).isEqualTo(command.name());
        assertThat(savedSeller.getProvider()).isEqualTo(command.provider());
        assertThat(savedSeller.getProviderId()).isEqualTo(command.providerId());

        assertThat(savedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
        assertThat(savedSeller.isDeleted()).isFalse();
        assertThat(savedSeller.getStore()).isNull();
    }

    @Test
    @DisplayName("Nickname만 있을 경우 Nickname을 사용해 OAuth2 판매자를 생성한다.")
    void success_create_OAuth2Seller_withNickname() {

        // given
        OAuth2ResponseCreateCommand command = OAuth2ResponseCreateCommand.builder()
                .name(null)
                .nickname("nickname")
                .provider(OauthServerType.KAKAO)
                .providerId("12345")
                .build();

        // when
        oAuth2SellerService.createOAuth2Seller(command);
        em.flush();
        em.clear();

        // then
        OAuth2Seller savedSeller = oAuth2SellerRepository.findAll().get(0);

        assertThat(savedSeller).isNotNull();
        assertThat(savedSeller.getName()).isEqualTo(command.nickname());
        assertThat(savedSeller.getProvider()).isEqualTo(command.provider());
        assertThat(savedSeller.getProviderId()).isEqualTo(command.providerId());

        assertThat(savedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
        assertThat(savedSeller.isDeleted()).isFalse();
        assertThat(savedSeller.getStore()).isNull();
    }
}