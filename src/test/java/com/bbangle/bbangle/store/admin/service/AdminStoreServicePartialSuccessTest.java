package com.bbangle.bbangle.store.admin.service;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_IDENTIFIER;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_IDENTIFIER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest.StoreApplicationApprove;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.admin.service.model.RegisterApproveResult;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[통합테스트] AdminStoreService")
@SpringBootTest
@ActiveProfiles("test")
class AdminStoreServicePartialSuccessTest {

    @Autowired
    private AdminStoreService adminStoreService;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreApplicationRepository storeApplicationRepository;

    @AfterEach
    void tearDown() {
        storeApplicationRepository.deleteAll();
        sellerRepository.deleteAll();
        storeRepository.deleteAll();
    }

    @Test
    @DisplayName("store가 없으면 createStore 후 승인 처리")
    void success_registerApprove_createStore() {

        // given
        Seller seller = sellerRepository.save(SellerFixture.defaultSeller(CertificationStatus.PENDING));
        StoreApplication application = storeApplicationRepository
            .save(StoreApplicationFixture.defaultStoreApplication(seller, null));
        StoreApplicationApprove command = StoreApplicationApprove.builder()
            .applicationId(application.getId())
            .identifier(NEW_IDENTIFIER)
            .sellerName("newSellerName")
            .build();

        // when
        RegisterApproveResult result = adminStoreService.registerApprove(application.getId(), command);

        // then
        Store savedStore = storeRepository.findById(result.store().getId()).orElseThrow();
        Seller savedSeller = sellerRepository.findById(seller.getId()).orElseThrow();
        StoreApplication savedApplication = storeApplicationRepository.findById(application.getId()).orElseThrow();

        assertThat(savedStore.getId()).isNotNull();
        assertThat(savedStore.getIdentifier()).isEqualTo(NEW_IDENTIFIER);
        assertThat(savedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.APPROVED);
        assertThat(savedSeller.getStore().getId()).isEqualTo(savedStore.getId());
        assertThat(savedApplication.getStatus()).isEqualTo(StoreApprovalStatus.APPROVE);
    }

    @Test
    @DisplayName("store가 있으면 updateStore 후 승인 처리")
    void success_registerApprove_updateStore() {

        // given
        Store store = storeRepository.save(
            Store.createForSeller(
                "oldName",
                "oldProfile",
                "oldIntroduce",
                DEFAULT_IDENTIFIER,
                "01011112222",
                "01099998888",
                "old@test.com",
                "oldAddress",
                "oldAddressDetail"
            )
        );
        Seller seller = sellerRepository.save(SellerFixture.defaultSeller(CertificationStatus.PENDING));
        StoreApplication application = storeApplicationRepository.save(
            StoreApplicationFixture.defaultStoreApplication(seller, store)
        );
        StoreApplicationApprove command = StoreApplicationApprove.builder()
            .applicationId(application.getId())
            .identifier(NEW_IDENTIFIER)
            .sellerName("updatedSeller")
            .build();


        // when
        adminStoreService.registerApprove(application.getId(), command);

        // then
        Store updatedStore = storeRepository.findById(store.getId()).orElseThrow();
        Seller updatedSeller = sellerRepository.findById(seller.getId()).orElseThrow();
        StoreApplication updatedApplication = storeApplicationRepository.findById(application.getId()).orElseThrow();

        assertThat(updatedStore.getId()).isEqualTo(store.getId());
        assertThat(updatedStore.getIdentifier()).isEqualTo(NEW_IDENTIFIER);
        assertThat(updatedStore.getIntroduce()).isEqualTo(application.getIntroduce());
        assertThat(updatedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.APPROVED);
        assertThat(updatedSeller.getStore().getId()).isEqualTo(updatedStore.getId());
        assertThat(updatedApplication.getStatus()).isEqualTo(StoreApprovalStatus.APPROVE);
    }

    @Test
    @DisplayName("application이 없으면 예외 발생")
    void fail_registerApprove_notFound() {

        // given
        long invalidId = 99999L;

        StoreApplicationApprove command = StoreApplicationApprove.builder()
            .applicationId(invalidId)
            .identifier(NEW_IDENTIFIER)
            .sellerName("seller")
            .build();

        // when & then
        assertThatThrownBy(() -> adminStoreService.registerApprove(invalidId, command)
        )
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> {
                BbangleException ex = (BbangleException) e;
                assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.NOT_FOUND_REQUEST);
            });
    }
}