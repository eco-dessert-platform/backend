package com.bbangle.bbangle.seller.admin.facade;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest.StoreApplicationApprove;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationApproveList;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[통합테스트] AdminSellerFacade")
@SpringBootTest
@ActiveProfiles("test")
class AdminSellerFacadePartialSuccessTest {

    @Autowired
    private AdminSellerFacade adminSellerFacade;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreApplicationRepository storeApplicationRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @AfterEach
    void tearDown() {
        storeApplicationRepository.deleteAll();
        sellerRepository.deleteAll();
        storeRepository.deleteAll();
    }

    @Test
    @DisplayName("여러 요청 중 일부 실패해도 성공 건은 반영된다")
    void approveStoreApplications_partialSuccess() {

        // given
        Seller successSeller = sellerRepository.save(SellerFixture.defaultSeller(CertificationStatus.PENDING));
        StoreApplication successApplication = storeApplicationRepository.save(
            StoreApplicationFixture.defaultStoreApplication(successSeller, null)
        );

        // 이미 존재하는 store 생성
        storeRepository.save(
            Store.createForSeller(
                "중복상점",
                "profile",
                "introduce",
                "duplicate-id",
                "01012341234",
                null,
                "test@test.com",
                "address",
                "detail"
            )
        );

        Seller failSeller = sellerRepository.save(SellerFixture.defaultSeller(CertificationStatus.PENDING));
        StoreApplication failApplication = storeApplicationRepository
            .save(StoreApplicationFixture.defaultStoreApplication(failSeller, null));

        StoreApplicationApprove successRequest = StoreApplicationApprove.builder()
            .applicationId(successApplication.getId())
            .sellerName("success-seller")
            .identifier("success-identifier")
            .build();

        // duplicate identifier 유도
        StoreApplicationApprove failRequest = StoreApplicationApprove.builder()
            .applicationId(failApplication.getId())
            .sellerName("fail-seller")
            .identifier("duplicate-id")
            .build();

        // when
        AdminSellerApplicationApproveList response = adminSellerFacade.approveStoreApplications(List.of(successRequest, failRequest));

        em.clear();

        // then

        // response 검증
        assertThat(response.successDetails()).hasSize(1);
        assertThat(response.successDetails().get(0).storeApplicationId()).isEqualTo(successApplication.getId());
        assertThat(response.failDetails()).hasSize(1);
        assertThat(response.failDetails().get(0).storeApplicationId()).isEqualTo(failApplication.getId());
        assertThat(response.failDetails().get(0).reason()).isEqualTo(BbangleErrorCode.ALREADY_RESERVED_STORE.getMessage());

        // 성공 건 DB 반영 검증
        StoreApplication approvedApplication = storeApplicationRepository.findByIdWithDetails(successApplication.getId()).orElseThrow();

        assertThat(approvedApplication.getStatus()).isEqualTo(StoreApprovalStatus.APPROVE);
        assertThat(approvedApplication.getSeller().getStore()).isNotNull();
        assertThat(approvedApplication.getSeller().getCertificationStatus()).isEqualTo(CertificationStatus.APPROVED);

        // 실패 건 rollback 검증
        StoreApplication failedApplication = storeApplicationRepository.findByIdWithDetails(failApplication.getId()).orElseThrow();

        assertThat(failedApplication.getStatus()).isEqualTo(StoreApprovalStatus.PENDING);
        assertThat(failedApplication.getSeller().getStore()).isNull();
        assertThat(failedApplication.getSeller().getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
    }
}