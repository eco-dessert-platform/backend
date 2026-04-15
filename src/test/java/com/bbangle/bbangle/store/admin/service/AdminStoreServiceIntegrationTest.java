package com.bbangle.bbangle.store.admin.service;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.UpdateStoreNameRejectRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameApprove;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameReject;
import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.domain.model.StoreNameRejectCategory;
import com.bbangle.bbangle.store.repository.StoreNameRequestRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] AdminStoreService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminStoreServiceIntegrationTest {

    @Autowired
    private AdminStoreService adminStoreService;

    @Autowired
    private StoreNameRequestRepository storeNameRequestRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("getPendingRequests() 테스트")
    class GetPendingRequestsTest {

        @BeforeEach
        public void setUp() {
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller(store));

            for (int i = 0; i < 150; i++) {
                StoreNameRequest entity = StoreNameRequestFixture.defaultStoreNameRequest(seller, store);
                StoreNameRequest approved = StoreNameRequestFixture.defaultStoreNameRequest(seller, store, StoreApprovalStatus.APPROVE);

                storeNameRequestRepository.save(entity);
                storeNameRequestRepository.save(approved);
            }
            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("판매자의 스토어명 변경 요청 목록을 가져온다.")
        void success_getPendingRequests() {

            // given
            int page = 1;

            // when
            AdminStoreResponse.UpdateStoreNameRequest result = adminStoreService.getPendingRequests(page);

            // then
            List<UpdateStoreNames> dtos = result.updateStoreNames();

            assertThat(dtos).hasSize(100);
            assertThat(dtos).extracting(UpdateStoreNames::storeId).doesNotContainNull();
            assertThat(dtos).extracting(UpdateStoreNames::createdAt).doesNotContainNull();

            assertThat(result.totalElements()).isEqualTo(150);
            assertThat(result.totalPages()).isEqualTo(2);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("2 페이지 조회 시 나머지 50건을 가져온다.")
        void success_getPendingRequests_page2() {

            // given
            int page = 2;

            // when
            AdminStoreResponse.UpdateStoreNameRequest result = adminStoreService.getPendingRequests(page);

            // then
            List<UpdateStoreNames> dtos = result.updateStoreNames();

            assertThat(dtos).hasSize(50);
            assertThat(dtos).extracting(UpdateStoreNames::storeId).doesNotContainNull();
            assertThat(dtos).extracting(UpdateStoreNames::createdAt).doesNotContainNull();

            assertThat(result.totalElements()).isEqualTo(150);
            assertThat(result.totalPages()).isEqualTo(2);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.hasPrevious()).isTrue();
        }

        @Test
        @DisplayName("잘못된 Page 값이 들어오면 보정한다.")
        void success_getPendingRequests_pageSize() {

            // given
            int page = 0;

            // when
            AdminStoreResponse.UpdateStoreNameRequest result = adminStoreService.getPendingRequests(page);

            // then
            List<UpdateStoreNames> dtos = result.updateStoreNames();

            assertThat(dtos).hasSize(100);

            assertThat(result.totalElements()).isEqualTo(150);
            assertThat(result.totalPages()).isEqualTo(2);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.hasPrevious()).isFalse();
        }
    }

    @Nested
    @DisplayName("approveStoreName() 테스트")
    class ApproveStoreNameTest {

        @Test
        @DisplayName("스토어명 변경 승인에 성공한다.")
        void success_approveStoreName() {

            // given
            Store store = StoreFixture.defaultStore();
            storeRepository.save(store);

            Seller seller = sellerRepository.save(SellerFixture.defaultSeller(store));
            StoreNameRequest request = StoreNameRequestFixture.defaultStoreNameRequest(seller, store);
            storeNameRequestRepository.save(request);

            em.flush();
            em.clear();

            // when
            UpdateStoreNameApprove result = adminStoreService.approveStoreName(request.getId());

            // then
            Store updatedStore = storeRepository.findById(store.getId()).orElseThrow();
            StoreNameRequest updatedRequest = storeNameRequestRepository.findById(request.getId()).orElseThrow();

            assertThat(updatedStore.getName()).isEqualTo(NEW_STORE_NAME);
            assertThat(updatedRequest.getStatus()).isEqualTo(StoreApprovalStatus.APPROVE);
            assertThat(result.prevName()).isEqualTo(DEFAULT_STORE_NAME);
            assertThat(result.updateName()).isEqualTo(NEW_STORE_NAME);
            assertThat(result.status()).isEqualTo(StoreApprovalStatus.APPROVE);
            assertThat(result.modifiedAt()).isEqualTo(updatedStore.getModifiedAt());
        }

        @Test
        @DisplayName("이미 존재하는 스토어명일 경우 승인에 실패한다.")
        void fail_approveStoreName_alreadyExists() {

            // given
            Store store1 = StoreFixture.defaultStore("store1");
            Store store2 = StoreFixture.defaultStore(NEW_STORE_NAME);
            storeRepository.save(store1);
            storeRepository.save(store2);

            Seller seller = sellerRepository.save(SellerFixture.defaultSeller(store1));
            StoreNameRequest request = StoreNameRequestFixture.defaultStoreNameRequest(seller, store1);
            storeNameRequestRepository.save(request);

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> adminStoreService.approveStoreName(request.getId()))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_RESERVED_STORE);
                });

            Store unchangedStore = storeRepository.findById(store1.getId()).orElseThrow();
            StoreNameRequest unchangedRequest = storeNameRequestRepository.findById(request.getId()).orElseThrow();
            assertThat(unchangedStore.getName()).isEqualTo("store1");
            assertThat(unchangedRequest.getStatus()).isNotEqualTo(StoreApprovalStatus.APPROVE);
        }

        @Test
        @DisplayName("요청이 존재하지 않으면 승인에 실패한다.")
        void fail_approveStoreName_notFound() {

            // given
            long invalidId = 999L;

            // when & then
            assertThatThrownBy(() -> adminStoreService.approveStoreName(invalidId))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.NOT_FOUND_REQUEST);
                });
        }
    }

    @Nested
    @DisplayName("rejectStoreName() 테스트")
    class RejectStoreNameTest {

        @Test
        @DisplayName("스토어명 변경 요청 거절에 성공한다.")
        void success_rejectStoreName() {

            // given
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller(store));
            StoreNameRequest requestEntity = storeNameRequestRepository.save(
                StoreNameRequestFixture.defaultStoreNameRequest(seller, store)
            );

            em.flush();
            em.clear();

            UpdateStoreNameRejectRequest request =
                new UpdateStoreNameRejectRequest(StoreNameRejectCategory.ETC, StoreNameRejectCategory.ETC.getDescription());

            // when
            UpdateStoreNameReject result = adminStoreService.rejectStoreName(requestEntity.getId(), request);

            // then
            assertThat(result.status()).isEqualTo(StoreApprovalStatus.REJECT);
            assertThat(result.category()).isEqualTo(StoreNameRejectCategory.ETC);

            StoreNameRequest saved = storeNameRequestRepository.findById(requestEntity.getId()).orElseThrow();
            assertThat(saved.getStatus()).isEqualTo(StoreApprovalStatus.REJECT);
            assertThat(saved.getRejectCategory()).isEqualTo(StoreNameRejectCategory.ETC);
            assertThat(saved.getRejectDetail()).isEqualTo(StoreNameRejectCategory.ETC.getDescription());

            Store reloadedStore = storeRepository.findById(store.getId()).orElseThrow();
            assertThat(reloadedStore.getName()).isEqualTo(DEFAULT_STORE_NAME);
        }

        @Test
        @DisplayName("요청이 존재하지 않은 경우 거절에 실패한다.")
        void fail_rejectStoreName_notFound() {

            // given
            long invalidId = 999L;
            UpdateStoreNameRejectRequest request = new UpdateStoreNameRejectRequest(StoreNameRejectCategory.ETC, "중복");

            // when & then
            assertThatThrownBy(() -> adminStoreService.rejectStoreName(invalidId, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.NOT_FOUND_REQUEST);
                });
        }

        @ParameterizedTest
        @EnumSource(value = StoreApprovalStatus.class, names = {"APPROVE", "REJECT"})
        @DisplayName("이미 처리된 요청은 거절할 수 없다.")
        void fail_rejectStoreName_alreadyProcessed(StoreApprovalStatus status) {

            // given
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Seller seller = sellerRepository.save(SellerFixture.defaultSeller(store));

            StoreNameRequest entity = storeNameRequestRepository.save(
                StoreNameRequestFixture.defaultStoreNameRequest(seller, store, status)
            );

            em.flush();
            em.clear();

            BbangleErrorCode expected =
                status == StoreApprovalStatus.REJECT
                    ? BbangleErrorCode.REQUEST_IS_REJECTED
                    : BbangleErrorCode.REQUEST_IS_APPROVED;

            UpdateStoreNameRejectRequest request = new UpdateStoreNameRejectRequest(StoreNameRejectCategory.ETC, "중복");

            // when & then
            assertThatThrownBy(() -> adminStoreService.rejectStoreName(entity.getId(), request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(expected);
                });

            StoreNameRequest saved = storeNameRequestRepository.findById(entity.getId()).orElseThrow();
            assertThat(saved.getStatus()).isEqualTo(status);
            assertThat(saved.getRejectCategory()).isNull();
            assertThat(saved.getRejectDetail()).isNull();
        }
    }
}