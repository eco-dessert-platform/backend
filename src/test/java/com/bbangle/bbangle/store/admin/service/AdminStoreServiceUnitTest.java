package com.bbangle.bbangle.store.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse;
import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.repository.StoreNameRequestRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[단위테스트] AdminStoreService")
@ExtendWith(MockitoExtension.class)
class AdminStoreServiceUnitTest {

    @Mock
    private StoreNameRequestRepository storeNameRequestRepository;

    @InjectMocks
    private AdminStoreService adminStoreService;

    @Nested
    @DisplayName("getPendingRequests() 테스트")
    class GetPendingRequestsTest {

        private static final int DEFAULT_PAGE_SIZE = 100;

        private StoreNameRequest createEntity(Long id) {
            Store store = StoreFixture.defaultStore();
            ReflectionTestUtils.setField(store, "id", id);

            Seller seller = SellerFixture.defaultSeller(store);

            StoreNameRequest entity = StoreNameRequestFixture.defaultStoreNameRequest(seller, store);
            ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
            return entity;
        }

        @Test
        @DisplayName("판매자의 스토어명 변경 요청 목록을 가져온다.")
        void success_getPendingRequests() {
            // given
            int page = 1;

            StoreNameRequest entity1 = createEntity(1L);
            StoreNameRequest entity2 = createEntity(2L);
            List<StoreNameRequest> content = List.of(entity1, entity2);

            Page<StoreNameRequest> mockPage = new PageImpl<>(
                content,
                PageRequest.of(0, DEFAULT_PAGE_SIZE),
                content.size()
            );

            given(storeNameRequestRepository.findByStatus(
                eq(StoreApprovalStatus.PENDING), any(Pageable.class)
            )).willReturn(mockPage);

            // when
            AdminStoreResponse.UpdateStoreNameRequest result = adminStoreService.getPendingRequests(page);

            // then
            List<UpdateStoreNames> dtos = result.updateStoreNames();

            assertThat(dtos).hasSize(2);
            assertThat(dtos.get(0).storeId()).isEqualTo(1L);
            assertThat(dtos.get(1).storeId()).isEqualTo(2L);
            assertThat(dtos.get(0).createdAt()).isNotNull();

            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.hasPrevious()).isFalse();
        }

        @Test
        @DisplayName("잘못된 Page 값이 들어오면 보정한다.")
        void success_getPendingRequests_pageSize() {

            // given
            int page = 0;     // 잘못된 값

            Page<StoreNameRequest> mockPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, DEFAULT_PAGE_SIZE),
                0
            );

            given(storeNameRequestRepository.findByStatus(
                eq(StoreApprovalStatus.PENDING),
                any(Pageable.class)
            )).willReturn(mockPage);

            // when
            adminStoreService.getPendingRequests(page);

            // then
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

            verify(storeNameRequestRepository).findByStatus(eq(StoreApprovalStatus.PENDING), captor.capture());

            Pageable usedPageable = captor.getValue();

            assertThat(usedPageable.getPageNumber()).isEqualTo(0); // page=1로 보정 후 -1
            assertThat(usedPageable.getPageSize()).isEqualTo(DEFAULT_PAGE_SIZE); // max 100
        }
    }
}