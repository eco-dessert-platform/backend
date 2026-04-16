package com.bbangle.bbangle.store.admin.service;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture;
import com.bbangle.bbangle.seller.domain.Seller;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

    @Mock
    private StoreRepository storeRepository;

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
        void success_getPendingRequests_invalidPage() {

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

    @Nested
    @DisplayName("approveStoreName() 테스트")
    class ApproveStoreNameTest {

        @Test
        @DisplayName("스토어명 변경 승인 성공")
        void approveStoreName_success() {

            // given
            long requestId = 1L;

            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 10L);
            Seller seller = SellerFixture.defaultSeller(store);
            StoreNameRequest request = StoreNameRequestFixture.defaultStoreNameRequest(seller, store);

            given(storeNameRequestRepository.findById(requestId)).willReturn(Optional.of(request));
            given(storeRepository.findByStoreName(request.getNewName())).willReturn(Optional.empty());

            // when
            UpdateStoreNameApprove result = adminStoreService.approveStoreName(requestId);

            // then
            assertThat(result.storeId()).isEqualTo(10L);
            assertThat(result.prevName()).isEqualTo(DEFAULT_STORE_NAME);
            assertThat(result.updateName()).isEqualTo(NEW_STORE_NAME);
            assertThat(result.status()).isEqualTo(StoreApprovalStatus.APPROVE);
            assertThat(result.modifiedAt()).isEqualTo(store.getModifiedAt());
            assertThat(store.getName()).isEqualTo(NEW_STORE_NAME);
        }

        @Test
        @DisplayName("요청이 존재하지 않으면 승인에 실패한다.")
        void fail_approveStoreName_notFound() {

            // given
            long requestId = 1L;

            given(storeNameRequestRepository.findById(requestId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminStoreService.approveStoreName(requestId))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.NOT_FOUND_REQUEST);
                });
        }

        @Test
        @DisplayName("이미 존재하는 스토어명일 경우 승인에 실패한다.")
        void fail_approveStoreName_alreadyExists() {

            // given
            long requestId = 1L;

            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 10L);
            Seller seller = SellerFixture.defaultSeller(store);
            StoreNameRequest request = StoreNameRequestFixture.defaultStoreNameRequest(seller, store);

            given(storeNameRequestRepository.findById(requestId)).willReturn(Optional.of(request));
            given(storeRepository.findByStoreName(request.getNewName())).willReturn(Optional.of(store));

            // when & then
            assertThatThrownBy(() -> adminStoreService.approveStoreName(requestId))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_RESERVED_STORE);
                });
            assertThat(store.getName()).isEqualTo(DEFAULT_STORE_NAME);
        }

        @ParameterizedTest
        @EnumSource(value = StoreApprovalStatus.class, names = {"APPROVE", "REJECT"})
        @DisplayName("이미 처리된 요청은 승인할 수 없다.")
        void fail_approveStoreName_alreadyProcessed(StoreApprovalStatus status) {

            // given
            long requestId = 1L;

            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 10L);
            Seller seller = SellerFixture.defaultSeller(store);
            StoreNameRequest request = StoreNameRequestFixture.defaultStoreNameRequest(seller, store, status);

            given(storeNameRequestRepository.findById(requestId)).willReturn(Optional.of(request));

            BbangleErrorCode expected =
                status == StoreApprovalStatus.REJECT
                    ? BbangleErrorCode.REQUEST_IS_REJECTED
                    : BbangleErrorCode.REQUEST_IS_APPROVED;

            // when & then
            assertThatThrownBy(() -> adminStoreService.approveStoreName(requestId)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(expected);
                });

            assertThat(request.getStatus()).isEqualTo(status);
        }
    }

    @Nested
    @DisplayName("rejectStoreName() 테스트")
    class RejectStoreNameTest {

        @Test
        @DisplayName("스토어명 변경 요청 거절에 성공한다.")
        void success_rejectStoreName() {

            // given
            long requestId = 1L;

            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 10L);
            Seller seller = SellerFixture.defaultSeller(store);
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.withId(
                StoreNameRequestFixture.defaultStoreNameRequest(seller, store), requestId
            );

            UpdateStoreNameRejectRequest request =
                new UpdateStoreNameRejectRequest(StoreNameRejectCategory.ETC, StoreNameRejectCategory.ETC.getDescription());

            given(storeNameRequestRepository.findById(requestId)).willReturn(Optional.of(storeNameRequest));

            // when
            UpdateStoreNameReject result = adminStoreService.rejectStoreName(requestId, request);

            // then
            assertThat(result.requestId()).isEqualTo(requestId);
            assertThat(result.storeId()).isEqualTo(10L);
            assertThat(result.currentName()).isEqualTo(DEFAULT_STORE_NAME);
            assertThat(result.newName()).isEqualTo(NEW_STORE_NAME);
            assertThat(result.status()).isEqualTo(StoreApprovalStatus.REJECT);
            assertThat(result.category()).isEqualTo(StoreNameRejectCategory.ETC);
            assertThat(result.rejectDetail()).isEqualTo(StoreNameRejectCategory.ETC.getDescription());

            assertThat(storeNameRequest.getStatus()).isEqualTo(StoreApprovalStatus.REJECT);
            assertThat(storeNameRequest.getRejectCategory()).isEqualTo(StoreNameRejectCategory.ETC);
            assertThat(storeNameRequest.getRejectDetail()).isEqualTo(StoreNameRejectCategory.ETC.getDescription());
        }

        @Test
        @DisplayName("요청이 존재하지 않은 경우 거절에 실패한다.")
        void fail_rejectStoreName_notFound() {

            // given
            long requestId = 1L;

            UpdateStoreNameRejectRequest request = new UpdateStoreNameRejectRequest(StoreNameRejectCategory.ETC, "중복");

            given(storeNameRequestRepository.findById(requestId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminStoreService.rejectStoreName(requestId, request))
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
            long requestId = 1L;

            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 10L);
            Seller seller = SellerFixture.defaultSeller(store);
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.withId(
                StoreNameRequestFixture.defaultStoreNameRequest(seller, store, status), requestId
            );
            UpdateStoreNameRejectRequest request = new UpdateStoreNameRejectRequest(StoreNameRejectCategory.ETC, "중복");

            given(storeNameRequestRepository.findById(requestId)).willReturn(Optional.of(storeNameRequest));

            BbangleErrorCode expected =
                status == StoreApprovalStatus.REJECT
                    ? BbangleErrorCode.REQUEST_IS_REJECTED
                    : BbangleErrorCode.REQUEST_IS_APPROVED;

            // when & then
            assertThatThrownBy(() -> adminStoreService.rejectStoreName(requestId, request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(expected);
                });

            assertThat(storeNameRequest.getStatus()).isEqualTo(status);
            assertThat(storeNameRequest.getRejectCategory()).isNull();
            assertThat(storeNameRequest.getRejectDetail()).isNull();
        }

        @Test
        @DisplayName("rejectDetail이 null이어도 거절에 성공한다.")
        void success_rejectStoreName_withNullDetail() {

            // given
            long requestId = 1L;

            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 10L);
            Seller seller = SellerFixture.defaultSeller(store);
            StoreNameRequest storeNameRequest = StoreNameRequestFixture.withId(
                StoreNameRequestFixture.defaultStoreNameRequest(seller, store), requestId
            );

            // rejectDetail is null (nullable field)
            UpdateStoreNameRejectRequest request =
                new UpdateStoreNameRejectRequest(StoreNameRejectCategory.ETC, null);

            given(storeNameRequestRepository.findById(requestId)).willReturn(Optional.of(storeNameRequest));

            // when
            UpdateStoreNameReject result = adminStoreService.rejectStoreName(requestId, request);

            // then
            assertThat(result.status()).isEqualTo(StoreApprovalStatus.REJECT);
            assertThat(result.category()).isEqualTo(StoreNameRejectCategory.ETC);
            assertThat(result.rejectDetail()).isNull();

            assertThat(storeNameRequest.getStatus()).isEqualTo(StoreApprovalStatus.REJECT);
            assertThat(storeNameRequest.getRejectCategory()).isEqualTo(StoreNameRejectCategory.ETC);
            assertThat(storeNameRequest.getRejectDetail()).isNull();
        }
    }
}