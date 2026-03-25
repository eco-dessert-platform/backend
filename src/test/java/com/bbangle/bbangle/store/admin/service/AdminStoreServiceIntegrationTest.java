package com.bbangle.bbangle.store.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse;
import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.repository.StoreNameRequestRepository;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

            for (int i = 0; i < 12; i++) {
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
            int size = 10;

            // when
            AdminStoreResponse.UpdateStoreNameRequest result = adminStoreService.getPendingRequests(page, size);

            // then
            List<UpdateStoreNames> dtos = result.updateStoreNames();

            assertThat(dtos).hasSize(10);
            assertThat(dtos).extracting(UpdateStoreNames::storeId).doesNotContainNull();
            assertThat(dtos).extracting(UpdateStoreNames::createdAt).doesNotContainNull();

            assertThat(result.totalElements()).isEqualTo(12);
            assertThat(result.totalPages()).isEqualTo(2);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("잘못된 Page와 Size 값이 들어오면 보정한다.")
        void success_getPendingRequests_pageSize() {

            // given
            int page = 0;     // 잘못된 값
            int size = 200;   // 초과 값

            // when
            AdminStoreResponse.UpdateStoreNameRequest result = adminStoreService.getPendingRequests(page, size);

            // then
            List<UpdateStoreNames> dtos = result.updateStoreNames();

            // size=100으로 보정 → 실제 데이터 12개니까 12개 다 나와야 함
            assertThat(dtos).hasSize(12);

            assertThat(result.totalElements()).isEqualTo(12);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.hasPrevious()).isFalse();
        }
    }
}