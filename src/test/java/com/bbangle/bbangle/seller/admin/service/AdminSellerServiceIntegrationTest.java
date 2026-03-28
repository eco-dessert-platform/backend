package com.bbangle.bbangle.seller.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.fixture.seller.domain.AccountVerificationFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication;
import com.bbangle.bbangle.seller.domain.AccountVerification;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] AdminSellerService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminSellerServiceIntegrationTest {

    @Autowired
    private AdminSellerService adminSellerService;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("getAdminSellerApplicationList() 테스트")
    class GetAdminSellerApplicationListTest {

        private Seller createSeller() {
            Seller seller = SellerFixture.defaultSeller();
            em.persist(seller);
            return seller;
        }

        private void createAccountVerification(Seller seller, String accountNumber, boolean verified) {
            AccountVerification av = AccountVerificationFixture.defaultAccountVerification(seller, accountNumber, verified);
            em.persist(av);
        }

        private void createStoreApplication(Seller seller, String name, StoreApprovalStatus status) {
            StoreApplication sa = StoreApplicationFixture.defaultStoreApplication(name, seller, status);
            em.persist(sa);
        }

        @Test
        @DisplayName("판매자의 스토어 등록 신청 목록을 조회한다. - 첫 페이지 조회")
        void success_getAdminSellerApplicationList_firstPage() {

            // given
            Seller seller = createSeller();

            createAccountVerification(seller, "111", false);
            createAccountVerification(seller, "222", true);
            createAccountVerification(seller, "333", true); // 최신

            for (int i = 1; i <= 150; i++) {
                createStoreApplication(seller, "store" + i, StoreApprovalStatus.PENDING);
            }

            // 일부 APPROVE 추가 (필터링 확인)
            createStoreApplication(seller, "approvedStore", StoreApprovalStatus.APPROVE);

            em.flush();
            em.clear();

            // when
            AdminSellerResponse.AdminSellerApplicationList result = adminSellerService.getAdminSellerApplicationList(1);

            // then
            // ✅ 페이지 크기
            assertThat(result.adminSellerApplicationList()).hasSize(100);

            // ✅ total 계산
            assertThat(result.totalElements()).isEqualTo(150);
            assertThat(result.totalPages()).isEqualTo(2);

            // ✅ 페이징 상태
            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.hasNext()).isTrue();

            // ✅ 최신 verified=true 계좌 검증
            for (AdminSellerApplication app : result.adminSellerApplicationList()) {
                assertThat(app.sellerInfo().accountNumber()).isEqualTo("333");
            }
        }

        @Test
        @DisplayName("판매자의 스토어 등록 신청 목록을 조회한다. - 중간 페이지 조회")
        void success_getAdminSellerApplicationList_middlePage() {

            // given
            Seller seller = createSeller();

            createAccountVerification(seller, "999", true);

            for (int i = 1; i <= 150; i++) {
                createStoreApplication(seller, "store" + i, StoreApprovalStatus.PENDING);
            }

            em.flush();
            em.clear();

            // when
            AdminSellerResponse.AdminSellerApplicationList result = adminSellerService.getAdminSellerApplicationList(2);

            // then
            assertThat(result.adminSellerApplicationList()).hasSize(50);
            assertThat(result.totalElements()).isEqualTo(150);
            assertThat(result.totalPages()).isEqualTo(2);
            assertThat(result.hasPrevious()).isTrue();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("데이터가 없는 경우 빈 리스트를 반환한다.")
        void getAdminSellerApplicationList_empty() {

            // given
            Seller seller = createSeller();

            createAccountVerification(seller, "111", false);
            createStoreApplication(seller, "store1", StoreApprovalStatus.PENDING);

            em.flush();
            em.clear();

            // when
            AdminSellerResponse.AdminSellerApplicationList result = adminSellerService.getAdminSellerApplicationList(1);

            // then
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.hasPrevious()).isFalse();
        }
    }
}