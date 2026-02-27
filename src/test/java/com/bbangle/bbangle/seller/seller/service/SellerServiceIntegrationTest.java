package com.bbangle.bbangle.seller.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.auth.oauth.OauthServerType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@DisplayName("[통합테스트] SellerService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SellerServiceIntegrationTest {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("getSellerById() 테스트")
    class GetSellerByIdTest {

        @Test
        @DisplayName("Seller Id를 통해 판매자 계정을 조회한다.")
        void success() {

            // given
            Seller seller = SellerFixture.defaultSeller();
            Seller newSeller = sellerRepository.save(seller);
            em.flush();
            em.clear();

            // when
            Seller result = sellerService.getSellerById(newSeller.getId());

            // then
            assertThat(result.getName()).isEqualTo(seller.getName());
            assertThat(result.getProvider()).isEqualTo(seller.getProvider());
            assertThat(result.getProviderId()).isEqualTo(seller.getProviderId());
        }

        @Test
        @DisplayName("Seller Id를 통해 존재하지 않는 판매자 계정을 조회할 경우 예외를 던진다.")
        void fail() {

            // given
            Long invalidSellerId = 999L;

            // when & then
            assertThatThrownBy(() -> sellerService.getSellerById(invalidSellerId))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.SELLER_NOT_FOUND);
                });
        }
    }

    @Nested
    @DisplayName("findByProviderAndProviderId() 테스트")
    class FindByProviderAndProviderIdTest {

        @Test
        @DisplayName("provider + providerId로 판매자 계정을 조회한다.")
        void success() {

            // given
            Seller seller = SellerFixture.defaultSeller();
            sellerRepository.save(seller);
            em.flush();
            em.clear();

            // when
            Optional<Seller> result = sellerService.findByProviderAndProviderId(seller.getProvider(), seller.getProviderId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo(seller.getName());
            assertThat(result.get().getProvider()).isEqualTo(seller.getProvider());
            assertThat(result.get().getProviderId()).isEqualTo(seller.getProviderId());
        }

        @Test
        @DisplayName("provider + providerId로 존재하지 않는 판매자 계정을 조회한다.")
        void success_with_empty() {

            // when
            Optional<Seller> result = sellerService.findByProviderAndProviderId(OauthServerType.KAKAO, "NOT_EXIST");

            // then
            assertThat(result).isNotPresent();
        }
    }

    @Nested
    @DisplayName("createOAuth2Seller() 테스트")
    class CreateOAuth2SellerTest {

        @Test
        @DisplayName("Name이 있을 경우 Name을 사용해 판매자를 생성한다.")
        void success_withName() {

            // given
            SellerCreateCommand command = SellerCreateCommand.builder()
                .name("test")
                .nickname(null)
                .provider(OauthServerType.KAKAO)
                .providerId("12345")
                .build();

            // when
            sellerService.createOAuth2Seller(command);
            em.flush();
            em.clear();

            // then
            Seller savedSeller = sellerRepository.findByProviderAndProviderId(command.provider(), command.providerId()).orElseThrow();

            assertThat(savedSeller).isNotNull();
            assertThat(savedSeller.getName()).isEqualTo(command.name());
            assertThat(savedSeller.getProvider()).isEqualTo(command.provider());
            assertThat(savedSeller.getProviderId()).isEqualTo(command.providerId());
            assertThat(savedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.NEW);
            assertThat(savedSeller.isDeleted()).isFalse();
            assertThat(savedSeller.getStore()).isNull();
        }

        @Test
        @DisplayName("Nickname만 있을 경우 Nickname을 사용해 OAuth2 판매자를 생성한다.")
        void success_withNickname() {

            // given
            SellerCreateCommand command = SellerCreateCommand.builder()
                .name(null)
                .nickname("nickname")
                .provider(OauthServerType.KAKAO)
                .providerId("12345")
                .build();

            // when
            sellerService.createOAuth2Seller(command);
            em.flush();
            em.clear();

            // then
            Seller savedSeller = sellerRepository.findByProviderAndProviderId(command.provider(), command.providerId()).orElseThrow();

            assertThat(savedSeller).isNotNull();
            assertThat(savedSeller.getName()).isEqualTo(command.nickname());
            assertThat(savedSeller.getProvider()).isEqualTo(command.provider());
            assertThat(savedSeller.getProviderId()).isEqualTo(command.providerId());
            assertThat(savedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.NEW);
            assertThat(savedSeller.isDeleted()).isFalse();
            assertThat(savedSeller.getStore()).isNull();
        }
    }

    @Nested
    @DisplayName("existsSellerByStoreId() 테스트")
    class ExistsSellerByStoreIdTest {

        @Test
        @DisplayName("해당 Store를 등록한 판매자가 있을 경우 true를 반환한다.")
        void already_registered() {

            // given
            Store store = storeRepository.saveAndFlush(StoreFixture.defaultStore());
            sellerRepository.saveAndFlush(SellerFixture.defaultSeller(store));

            // when
            boolean result = sellerService.existsSellerByStoreId(store.getId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("해당 Store를 등록한 판매자가 없을 경우 false를 반환한다.")
        void not_registered() {

            // given
            Long notExistStoreId = 999L;

            // when
            boolean result = sellerService.existsSellerByStoreId(notExistStoreId);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("updateSellerStatus() 테스트")
    class UpdateSellerStatusTest {

        @Test
        @DisplayName("Seller의 Status가 정상적으로 업데이트된다.")
        void success_updateSellerStatus() {

            // given
            Seller seller = sellerRepository.saveAndFlush(SellerFixture.defaultSeller());

            // when
            sellerService.updateSellerStatus(seller, CertificationStatus.APPROVED);
            em.flush();
            em.clear();

            Seller updateSeller = sellerRepository.findById(seller.getId()).orElseThrow();

            // then
            assertThat(updateSeller.getCertificationStatus()).isEqualTo(CertificationStatus.APPROVED);
        }
    }
}
