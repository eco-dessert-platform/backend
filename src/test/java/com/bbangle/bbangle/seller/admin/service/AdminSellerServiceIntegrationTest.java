package com.bbangle.bbangle.seller.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.fixture.seller.domain.AccountVerificationFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo;
import com.bbangle.bbangle.seller.domain.AccountVerification;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import jakarta.persistence.EntityManager;
import java.util.List;
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

            createAccountVerification(seller, "111", true); // 최신

            for (int i = 1; i <= 150; i++) {
                createStoreApplication(seller, "store" + i, StoreApprovalStatus.PENDING);
            }

            // 일부 APPROVE 추가 (필터링 확인)
            createStoreApplication(seller, "approvedStore", StoreApprovalStatus.APPROVE);

            em.flush();
            em.clear();

            // when
            SellerApplicationInfoList result = adminSellerService.getAdminSellerApplicationList(1);

            // then
            // ✅ 페이지 크기
            assertThat(result.sellerApplicationInfoList()).hasSize(100);

            // ✅ total 계산
            assertThat(result.totalElements()).isEqualTo(150);
            assertThat(result.totalPages()).isEqualTo(2);

            // ✅ 페이징 상태
            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.hasNext()).isTrue();

            for (SellerApplicationInfo app : result.sellerApplicationInfoList()) {
                assertThat(app.sellerInfo().accountNumber()).isEqualTo("111");
            }

            List<SellerApplicationInfo> list = result.sellerApplicationInfoList();
            for (int i = 0; i < list.size() - 1; i++) {
                assertThat(list.get(i).storeApplicationId()).isLessThan(list.get(i + 1).storeApplicationId());
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
            SellerApplicationInfoList result = adminSellerService.getAdminSellerApplicationList(2);

            // then
            assertThat(result.sellerApplicationInfoList()).hasSize(50);
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
            SellerApplicationInfoList result = adminSellerService.getAdminSellerApplicationList(1);

            // then
            assertThat(result.totalPages()).isEqualTo(0);
            assertThat(result.totalElements()).isEqualTo(0);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.sellerApplicationInfoList()).isEmpty();
        }

        @Test
        @DisplayName("승인 대기 중인 데이터가 없으면 결과는 빈 리스트를 반환한다")
        void getAdminSellerApplicationList_noExist_pending() {

            Seller seller = createSeller();
            createAccountVerification(seller, "111", true);

            createStoreApplication(seller, "store1", StoreApprovalStatus.APPROVE);

            em.flush();
            em.clear();

            SellerApplicationInfoList result = adminSellerService.getAdminSellerApplicationList(1);

            assertThat(result.totalElements()).isEqualTo(0);
            assertThat(result.totalPages()).isEqualTo(0);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.sellerApplicationInfoList()).isEmpty();
        }

        @Test
        @DisplayName("조회할 페이지가 전체 페이지보다 크면 빈 리스트 반환한다")
        void getAdminSellerApplicationList_page_overflow() {

            Seller seller = createSeller();
            createAccountVerification(seller, "111", true);

            for (int i = 1; i <= 150; i++) {
                createStoreApplication(seller, "store" + i, StoreApprovalStatus.PENDING);
            }

            em.flush();
            em.clear();

            SellerApplicationInfoList result = adminSellerService.getAdminSellerApplicationList(3);

            assertThat(result.sellerApplicationInfoList()).isEmpty();
            assertThat(result.hasNext()).isFalse();
            assertThat(result.hasPrevious()).isTrue();
        }
    }
}