package com.bbangle.bbangle.seller.admin.service;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.fixture.seller.domain.AccountVerificationFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationRejectList;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo;
import com.bbangle.bbangle.seller.domain.AccountVerification;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
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

    @Autowired
    private StoreApplicationRepository storeApplicationRepository;

    @Autowired
    private SellerRepository sellerRepository;

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

    @Nested
    @DisplayName("rejectStoreApplications() 테스트")
    class RejectStoreApplicationsTest {

        @Test
        @DisplayName("모든 신청이 정상적으로 거절된다")
        void success_rejectStoreApplications() {

            // given
            Seller seller1 = sellerRepository.save(SellerFixture.defaultSeller());
            Seller seller2 = sellerRepository.save(SellerFixture.defaultSeller());
            StoreApplication app1 = storeApplicationRepository.save(
                StoreApplicationFixture.defaultStoreApplication(seller1, null)
            );
            StoreApplication app2 = storeApplicationRepository.save(
                StoreApplicationFixture.defaultStoreApplication(seller2, null)
            );
            em.flush();
            em.clear();

            List<Long> ids = List.of(app1.getId(), app2.getId());

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);
            em.flush();
            em.clear();

            // then
            assertThat(result.successIds()).containsExactlyInAnyOrderElementsOf(ids);
            assertThat(result.failDetails()).isEmpty();

            // DB 반영 확인 (핵심)
            StoreApplication updated1 = storeApplicationRepository.findById(app1.getId()).orElseThrow();
            StoreApplication updated2 = storeApplicationRepository.findById(app2.getId()).orElseThrow();

            assertThat(updated1.getStatus()).isEqualTo(StoreApprovalStatus.REJECT);
            assertThat(updated2.getStatus()).isEqualTo(StoreApprovalStatus.REJECT);

            assertThat(updated1.getSeller().getCertificationStatus()).isEqualTo(CertificationStatus.REJECTED);
            assertThat(updated2.getSeller().getCertificationStatus()).isEqualTo(CertificationStatus.REJECTED);
        }

        @Test
        @DisplayName("존재하지 않는 신청의 ID는 실패로 처리된다")
        void rejectStoreApplications_with_not_found() {

            // given
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller());
            StoreApplication app = storeApplicationRepository.save(
                StoreApplicationFixture.defaultStoreApplication(seller, null)
            );
            em.flush();
            em.clear();

            List<Long> ids = List.of(app.getId(), 999L);

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);
            em.flush();
            em.clear();

            // then
            assertThat(result.successIds()).containsExactly(app.getId());
            assertThat(result.failDetails()).hasSize(1);

            StoreApplication persisted = storeApplicationRepository.findById(app.getId()).orElseThrow();
            assertThat(persisted.getStatus()).isEqualTo(StoreApprovalStatus.REJECT);
            assertThat(persisted.getSeller().getCertificationStatus()).isEqualTo(CertificationStatus.REJECTED);

            assertThat(result.failDetails().get(0).storeApplicationId()).isEqualTo(999L);
        }

        @Test
        @DisplayName("이미 승인된 요청은 거절 시 실패 처리된다.")
        void rejectStoreApplications_business_exception() {

            // given
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller(CertificationStatus.APPROVED));
            StoreApplication app = storeApplicationRepository.save(
                StoreApplicationFixture.defaultStoreApplication(DEFAULT_STORE_NAME, seller, StoreApprovalStatus.APPROVE)
            );
            em.flush();
            em.clear();

            List<Long> ids = List.of(app.getId());

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);
            em.flush();
            em.clear();

            // then
            assertThat(result.successIds()).isEmpty();
            assertThat(result.failDetails()).hasSize(1);

            assertThat(result.failDetails().get(0).reason()).isEqualTo(BbangleErrorCode.REQUEST_IS_APPROVED.getMessage());

            // DB 상태 그대로 유지 확인
            StoreApplication persisted = storeApplicationRepository.findById(app.getId()).orElseThrow();
            assertThat(persisted.getStatus()).isEqualTo(StoreApprovalStatus.APPROVE);
            assertThat(persisted.getSeller().getCertificationStatus()).isEqualTo(CertificationStatus.APPROVED);
        }

        @Test
        @DisplayName("모든 ID가 존재하지 않으면 성공은 없고 모두 실패 처리된다.")
        void rejectStoreApplications_all_not_found() {

            // given
            List<Long> ids = List.of(9991L, 9992L, 9993L);

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);

            // then
            assertThat(result.successIds()).isEmpty();
            assertThat(result.failDetails()).hasSize(3);
            assertThat(result.failDetails())
                .extracting(AdminSellerApplicationRejectList.FailDetail::reason)
                .containsOnly(BbangleErrorCode.NOT_FOUND_REQUEST.getMessage());
            assertThat(result.failDetails())
                .extracting(AdminSellerApplicationRejectList.FailDetail::storeApplicationId)
                .containsExactlyInAnyOrder(9991L, 9992L, 9993L);
        }

        @Test
        @DisplayName("거절 상태인 신청을 다시 거절해도 정상 처리된다.")
        void rejectStoreApplications_already_rejected_application() {

            // given
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller(CertificationStatus.REJECTED));
            StoreApplication app = storeApplicationRepository.save(
                StoreApplicationFixture.defaultStoreApplication(DEFAULT_STORE_NAME, seller, StoreApprovalStatus.REJECT)
            );
            em.flush();
            em.clear();

            List<Long> ids = List.of(app.getId());

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);
            em.flush();
            em.clear();

            // then
            assertThat(result.successIds()).containsExactly(app.getId());
            assertThat(result.failDetails()).isEmpty();

            StoreApplication persisted = storeApplicationRepository.findById(app.getId()).orElseThrow();
            assertThat(persisted.getStatus()).isEqualTo(StoreApprovalStatus.REJECT);
        }
    }
}