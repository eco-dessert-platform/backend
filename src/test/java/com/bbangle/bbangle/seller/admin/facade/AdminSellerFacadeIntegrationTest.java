package com.bbangle.bbangle.seller.admin.facade;

import static com.bbangle.bbangle.fixture.seller.domain.AccountVerificationFixture.DEFAULT_BANK_CODE;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.fixture.seller.domain.AccountVerificationFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.AccountVerificationRepository;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
import com.bbangle.bbangle.util.AesEncryptionUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] AdminSellerFacade")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminSellerFacadeIntegrationTest {

    @Autowired
    private AdminSellerFacade adminSellerFacade;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreApplicationRepository storeApplicationRepository;

    @Autowired
    private AccountVerificationRepository accountVerificationRepository;

    @Autowired
    private AesEncryptionUtil aesEncryptionUtil;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("getAdminSellerApplicationList() 테스트")
    class GetAdminSellerApplicationListTest {

        @Test
        @DisplayName("판매자 스토어 등록 신청 목록을 조회한다.")
        void success_getAdminSellerApplicationList() {

            // given
            String decryptAccountNumber = "111-2222";
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller());
            StoreApplication storeApplication = storeApplicationRepository.save(
                StoreApplicationFixture.defaultStoreApplication(seller, null));
            accountVerificationRepository.save(
                AccountVerificationFixture.defaultAccountVerification(seller, aesEncryptionUtil.encrypt(decryptAccountNumber), true)
            );
            em.flush();
            em.clear();

            // when
            AdminSellerResponse.AdminSellerApplicationList result = adminSellerFacade.getAdminSellerApplicationList(1);

            // then
            assertThat(result).isNotNull();
            assertThat(result.adminSellerApplicationList()).hasSize(1);

            AdminSellerApplication response = result.adminSellerApplicationList().get(0);

            assertThat(response.storeApplicationId()).isEqualTo(storeApplication.getId());
            assertThat(response.sellerDTO().accountNumber()).isEqualTo(decryptAccountNumber);
            assertThat(response.sellerDTO().bankCode()).isEqualTo(DEFAULT_BANK_CODE);
            assertThat(response.sellerStoreDTO().storeName()).isEqualTo(DEFAULT_STORE_NAME);
        }

        @Test
        @DisplayName("데이터 없을 경우 빈 리스트를 반환한다.")
        void getAdminSellerApplicationList_empty() {

            // when
            AdminSellerResponse.AdminSellerApplicationList result = adminSellerFacade.getAdminSellerApplicationList(1);

            // then
            assertThat(result.adminSellerApplicationList()).isEmpty();
            assertThat(result.totalElements()).isEqualTo(0);
        }
    }
}