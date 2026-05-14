package com.bbangle.bbangle.store.admin.service;

import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_IDENTIFIER;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.DEFAULT_STORE_NAME;
import static com.bbangle.bbangle.fixture.store.domain.StoreFixture.NEW_IDENTIFIER;
import static com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture.NEW_STORE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.admin.service.model.AdminStoreInfoFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreNameRequestFixture;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest.StoreApplicationApprove;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreRequest.UpdateStoreNameRejectRequest;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameApprove;
import com.bbangle.bbangle.store.admin.controller.dto.AdminStoreResponse.UpdateStoreNameReject;
import com.bbangle.bbangle.store.admin.service.model.AdminStoreInfo;
import com.bbangle.bbangle.store.admin.service.model.RegisterApproveResult;
import com.bbangle.bbangle.store.admin.service.model.UpdateStoreNamesInfo.UpdateStoreNames;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.domain.StoreNameRequest;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.domain.model.StoreNameRejectCategory;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
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

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private StoreApplicationRepository storeApplicationRepository;

    @Mock
    private AdminStoreMapper adminStoreMapper;

    @InjectMocks
    private AdminStoreService adminStoreService;

    @Nested
    @DisplayName("searchStoresByName() 테스트")
    class SearchStoresByNameTest {

        private static final int SEARCH_PAGE_SIZE = 20;

        @Test
        @DisplayName("storeName이 null이면 빈 문자열로 정규화하여 전체 활성 스토어를 조회한다")
        void success_searchStoresByName_nullName() {
            // given
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 1L);
            Page<Store> mockPage = new PageImpl<>(
                List.of(store),
                PageRequest.of(0, SEARCH_PAGE_SIZE),
                1
            );
            ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
            given(storeRepository.findActiveStoresByName(any(), any(Pageable.class))).willReturn(mockPage);

            // when
            adminStoreService.searchStoresByName(null, 1);

            // then
            verify(storeRepository).findActiveStoresByName(nameCaptor.capture(), any());
            assertThat(nameCaptor.getValue()).isEmpty();
        }

        @Test
        @DisplayName("storeName에 공백이 있으면 제거 후 검색한다")
        void success_searchStoresByName_withSpaces() {
            // given
            Page<Store> mockPage = new PageImpl<>(List.of(), PageRequest.of(0, SEARCH_PAGE_SIZE), 0);
            ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
            given(storeRepository.findActiveStoresByName(any(), any(Pageable.class))).willReturn(mockPage);

            // when
            adminStoreService.searchStoresByName("빵 그 리", 1);

            // then
            verify(storeRepository).findActiveStoresByName(nameCaptor.capture(), any());
            assertThat(nameCaptor.getValue()).isEqualTo("빵그리");
        }

        @Test
        @DisplayName("page 0 이하 입력 시 1로 보정하여 0-base index로 전달한다")
        void success_searchStoresByName_invalidPage() {
            // given
            Page<Store> mockPage = new PageImpl<>(List.of(), PageRequest.of(0, SEARCH_PAGE_SIZE), 0);
            given(storeRepository.findActiveStoresByName(any(), any(Pageable.class))).willReturn(mockPage);

            // when
            adminStoreService.searchStoresByName("test", 0);

            // then
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(storeRepository).findActiveStoresByName(any(), pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0); // page=1로 보정 후 -1 = 0
        }

        @Test
        @DisplayName("검색 결과의 페이지 정보가 응답 DTO에 정확히 매핑된다")
        void success_searchStoresByName_responseMapping() {
            // given
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 1L);
            Page<Store> mockPage = new PageImpl<>(
                List.of(store),
                PageRequest.of(0, SEARCH_PAGE_SIZE),
                1
            );
            given(storeRepository.findActiveStoresByName(any(), any(Pageable.class))).willReturn(mockPage);

            // when
            StoreSearchResult result = adminStoreService.searchStoresByName("test", 1);

            // then
            assertThat(result.storeSummaries()).hasSize(1);
            assertThat(result.storeSummaries().get(0).id()).isEqualTo(1L);
            assertThat(result.storeSummaries().get(0).name()).isEqualTo(StoreFixture.DEFAULT_STORE_NAME);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        @DisplayName("검색 결과가 없으면 빈 목록과 totalElements=0을 반환한다")
        void success_searchStoresByName_emptyResult() {
            // given
            Page<Store> mockPage = new PageImpl<>(List.of(), PageRequest.of(0, SEARCH_PAGE_SIZE), 0);
            given(storeRepository.findActiveStoresByName(any(), any(Pageable.class))).willReturn(mockPage);

            // when
            StoreSearchResult result = adminStoreService.searchStoresByName("없는스토어", 1);

            // then
            assertThat(result.storeSummaries()).isEmpty();
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.hasNext()).isFalse();
        }
    }

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
    }

    @Nested
    @DisplayName("createStore() 테스트")
    class CreateStoreTest {

        @Test
        @DisplayName("스토어 생성에 성공한다")
        void success_createStore() {

            // given
            Seller seller = SellerFixture.defaultSeller();
            StoreApplication storeApplication = StoreApplicationFixture.defaultStoreApplication(seller, null);
            Store store = StoreFixture.defaultStore();

            given(storeRepository.existsByName(storeApplication.getName())).willReturn(false);
            given(storeRepository.save(any(Store.class))).willReturn(store);

            // when
            Store result = adminStoreService.createStore(
                AdminStoreInfoFixture.withStoreApplication(storeApplication, DEFAULT_IDENTIFIER)
            );

            // then
            assertThat(result).isEqualTo(store);
            assertThat(result.getName()).isEqualTo(storeApplication.getName());
            assertThat(result.getIdentifier()).isEqualTo(DEFAULT_IDENTIFIER);

            verify(storeRepository, times(1)).save(any(Store.class));
        }

        @Test
        @DisplayName("스토어 이름이 중복되면 예외가 발생한다")
        void fail_createStore_duplicateName() {

            // given
            Seller seller = SellerFixture.defaultSeller();
            StoreApplication storeApplication = StoreApplicationFixture.defaultStoreApplication(seller, null);
            AdminStoreInfo adminStoreInfo = AdminStoreInfoFixture.withStoreApplication(storeApplication, DEFAULT_IDENTIFIER);

            given(storeRepository.existsByName(adminStoreInfo.storeName())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> adminStoreService.createStore(adminStoreInfo)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_RESERVED_STORE);
                });

            verify(storeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateStore() 테스트")
    class UpdateStoreTest {

        @Test
        @DisplayName("스토어 수정에 성공한다.")
        void success_updateStore() {

            // given
            String newIdentifier = "new-identifier";

            Store store = StoreFixture.defaultStore();
            Seller seller = SellerFixture.defaultSeller();
            StoreApplication storeApplication = StoreApplicationFixture.defaultStoreApplication(seller, store);
            AdminStoreInfo adminStoreInfo = AdminStoreInfoFixture.withStoreApplication(storeApplication, newIdentifier);

            // when
            Store result = adminStoreService.updateStore(adminStoreInfo, store);

            // then
            assertThat(result).isSameAs(store);
            verify(storeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("registerApprove() 테스트")
    class RegisterApproveTest {

        @Test
        @DisplayName("store가 없으면 createStore 호출 후 승인 처리")
        void success_registerApprove_createStore() {

            // given
            long applicationId = 1L;

            Seller seller = SellerFixture.defaultSeller();
            StoreApplication application = StoreApplicationFixture.defaultStoreApplication(seller, null);

            given(storeApplicationRepository.findByIdWithDetails(applicationId)).willReturn(Optional.of(application));
            given(storeRepository.save(any(Store.class))).willAnswer(invocation -> invocation.getArgument(0));

            StoreApplicationApprove command = StoreApplicationApprove.builder()
                .applicationId(applicationId)
                .identifier(DEFAULT_IDENTIFIER)
                .sellerName(seller.getName())
                .build();

            // when
            RegisterApproveResult result = adminStoreService.registerApprove(applicationId, command);

            // then
            assertThat(result.store()).isNotNull();
            assertThat(result.store().getIdentifier()).isEqualTo(DEFAULT_IDENTIFIER);
            assertThat(result.seller().getCertificationStatus()).isEqualTo(CertificationStatus.APPROVED);
            assertThat(result.storeApplication().getStatus()).isEqualTo(StoreApprovalStatus.APPROVE);
            assertThat(result.seller().getStore()).isEqualTo(result.store());
        }

        @Test
        @DisplayName("store가 있으면 updateStore 호출 후 승인 처리")
        void success_registerApprove_updateStore() {

            // given
            long applicationId = 1L;

            Store store = StoreFixture.defaultStore();
            Seller seller = SellerFixture.defaultSeller();
            StoreApplication application = StoreApplicationFixture.defaultStoreApplication(seller, store);

            given(storeApplicationRepository.findByIdWithDetails(applicationId)).willReturn(Optional.of(application));
            given(sellerRepository.existsByStore_Id(store.getId())).willReturn(false);

            StoreApplicationApprove command = StoreApplicationApprove.builder()
                .applicationId(applicationId)
                .identifier(NEW_IDENTIFIER)
                .sellerName(seller.getName())
                .build();

            // when
            RegisterApproveResult result = adminStoreService.registerApprove(applicationId, command);

            // then
            assertThat(result.store()).isSameAs(store);
            assertThat(result.store().getName()).isEqualTo(store.getName());
            assertThat(result.store().getIdentifier()).isEqualTo(NEW_IDENTIFIER);
            assertThat(result.store().getIntroduce()).isEqualTo(application.getIntroduce());
            assertThat(result.store().getProfile()).isEqualTo(application.getProfile());
            assertThat(result.seller().getCertificationStatus()).isEqualTo(CertificationStatus.APPROVED);
            assertThat(result.storeApplication().getStatus()).isEqualTo(StoreApprovalStatus.APPROVE);
            assertThat(result.seller().getStore()).isEqualTo(result.store());
        }

        @Test
        @DisplayName("등록 신청 정보가 없으면 예외 발생")
        void fail_registerApprove_notFound() {

            // given
            given(storeApplicationRepository.findByIdWithDetails(anyLong())).willReturn(Optional.empty());

            StoreApplicationApprove command = StoreApplicationApprove.builder()
                .applicationId(1L)
                .identifier(DEFAULT_IDENTIFIER)
                .sellerName("test")
                .build();

            // when & then
            assertThatThrownBy(() -> adminStoreService.registerApprove(1L, command)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.NOT_FOUND_REQUEST);
                });
        }

        @Test
        @DisplayName("이미 등록된 store를 다른 판매자가 등록할 경우 예외 발생")
        void fail_registerApprove_alreadyReserved() {

            // given
            long applicationId = 1L;

            Store store = StoreFixture.defaultStore();
            Seller seller = SellerFixture.defaultSeller();
            StoreApplication application = StoreApplicationFixture.defaultStoreApplication(seller, store);

            given(storeApplicationRepository.findByIdWithDetails(applicationId)).willReturn(Optional.of(application));
            given(sellerRepository.existsByStore_Id(store.getId())).willReturn(true);

            StoreApplicationApprove command = StoreApplicationApprove.builder()
                .applicationId(applicationId)
                .identifier(NEW_IDENTIFIER)
                .sellerName(seller.getName())
                .build();

            // when & then
            assertThatThrownBy(() -> adminStoreService.registerApprove(applicationId, command)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ALREADY_RESERVED_STORE);
                });
        }
    }

    @Nested
    @DisplayName("getRegisteredStores() 테스트")
    class GetRegisteredStoresTest {

        @Test
        @DisplayName("등록된 스토어 목록을 페이징 형태로 반환한다")
        void success_getRegisteredStores() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 1L);
            Page<Store> storePage = new PageImpl<>(List.of(store), pageable, 1);

            given(storeRepository.findAll(pageable)).willReturn(storePage);

            // when
            Page<RegisteredStoreInfo> result = adminStoreService.getRegisteredStores(pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            RegisteredStoreInfo info = result.getContent().get(0);
            assertThat(info.storeId()).isEqualTo(1L);
            assertThat(info.storeName()).isEqualTo(DEFAULT_STORE_NAME);
            assertThat(info.businessNumber()).isEqualTo(DEFAULT_IDENTIFIER);
            assertThat(result.getTotalElements()).isEqualTo(1);
            then(storeRepository).should().findAll(pageable);
        }

        @Test
        @DisplayName("등록된 스토어가 없으면 빈 Page를 반환한다")
        void emptyResult_getRegisteredStores() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            Page<Store> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(storeRepository.findAll(pageable)).willReturn(emptyPage);

            // when
            Page<RegisteredStoreInfo> result = adminStoreService.getRegisteredStores(pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            then(storeRepository).should().findAll(pageable);
        }
    }

    @Nested
    @DisplayName("deleteStores() 테스트")
    class DeleteStoresTest {

        @Test
        @DisplayName("모든 storeId가 유효하면 seller 관계를 끊고 상태를 NEW로 변경한 뒤 hard delete한다")
        void success_deleteStores() {
            // given
            List<Long> storeIds = List.of(1L, 2L, 3L);
            given(storeRepository.countByIdIn(storeIds)).willReturn(3L);

            // when
            adminStoreService.deleteStores(storeIds);

            // then
            then(storeRepository).should().countByIdIn(storeIds);
            then(sellerRepository).should().clearStoreAndResetStatusByStoreIdIn(storeIds, CertificationStatus.NEW);
            then(storeRepository).should().deleteAllByIdInBatch(storeIds);
        }

        @Test
        @DisplayName("존재하지 않는 storeId가 포함되면 STORE_NOT_FOUND 예외가 발생한다")
        void fail_deleteStores_notFound() {
            // given
            List<Long> storeIds = List.of(1L, 2L, 999L);
            given(storeRepository.countByIdIn(storeIds)).willReturn(2L);

            // when & then
            assertThatThrownBy(() -> adminStoreService.deleteStores(storeIds))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.STORE_NOT_FOUND);
                });

            then(storeRepository).should().countByIdIn(storeIds);
            then(sellerRepository).should(never()).clearStoreAndResetStatusByStoreIdIn(any(), any());
            then(storeRepository).should(never()).deleteAllByIdInBatch(any());
        }
    }

    @Nested
    @DisplayName("updateStoreWithName() 테스트")
    class UpdateStoreWithNameTest {

        @Test
        @DisplayName("스토어 상세 정보 수정에 성공한다")
        void success_update_store() {

            // given
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 1L);
            StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();
            StoreDetailResponse response = mock(StoreDetailResponse.class);

            given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));
            given(storeRepository.existsByStoreName(NEW_STORE_NAME)).willReturn(false);
            given(adminStoreMapper.toStoreDetailResponse(store)).willReturn(response);

            // when
            StoreDetailResponse result = adminStoreService.updateStoreWithName(store.getId(), request);

            // then
            assertThat(result).isEqualTo(response);
            assertThat(store.getName()).isEqualTo(NEW_STORE_NAME);
            assertThat(store.getIdentifier()).isEqualTo(NEW_IDENTIFIER);
            assertThat(store.getProfile()).isEqualTo(DEFAULT_PROFILE);

            verify(storeRepository).findById(store.getId());
            verify(storeRepository).existsByStoreName(NEW_STORE_NAME);
            verify(adminStoreMapper).toStoreDetailResponse(store);
        }

        @Test
        @DisplayName("스토어 이름이 동일하면 중복 체크를 하지 않는다")
        void success_same_store_name() {

            // given
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 1L);
            StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture(store.getName());
            StoreDetailResponse response = mock(StoreDetailResponse.class);

            given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));
            given(adminStoreMapper.toStoreDetailResponse(store)).willReturn(response);

            // when
            adminStoreService.updateStoreWithName(store.getId(), request);

            // then
            assertThat(store.getName()).isEqualTo(request.storeName());
            assertThat(store.getIdentifier()).isEqualTo(NEW_IDENTIFIER);
            assertThat(store.getProfile()).isEqualTo(DEFAULT_PROFILE);

            verify(storeRepository, never()).existsByStoreName(any());
        }

        @Test
        @DisplayName("스토어가 존재하지 않으면 예외가 발생한다")
        void fail_store_not_found() {

            // given
            StoreDetailRequest request = mock(StoreDetailRequest.class);

            given(storeRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminStoreService.updateStoreWithName(1L, request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.STORE_NOT_FOUND);
                });

            verify(storeRepository, never()).existsByStoreName(any());
            verify(adminStoreMapper, never()).toStoreDetailResponse(any());
        }

        @Test
        @DisplayName("스토어 이름이 중복되면 예외가 발생한다")
        void fail_duplicate_store_name() {

            // given
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 1L);
            StoreDetailRequest request = StoreDetailRequestFixture.defaultStoreDetailRequestFixture();

            given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));
            given(storeRepository.existsByStoreName(NEW_STORE_NAME)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> adminStoreService.updateStoreWithName(store.getId(), request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_STORE_NAME);
                });

            verify(adminStoreMapper, never()).toStoreDetailResponse(any());
        }

        @Test
        @DisplayName("스토어 수정 중 예외가 발생하면 그대로 전파된다")
        void fail_update_store() {

            // given
            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 1L);
            StoreDetailRequest request = new StoreDetailRequest(
                NEW_STORE_NAME,
                NEW_IDENTIFIER,
                NEW_INTRODUCE,
                "invalid-phone",
                NEW_SUBPHONE,
                NEW_EMAIL,
                NEW_ADDRESS,
                NEW_DETAIL_ADDRESS
            );

            given(storeRepository.findById(store.getId())).willReturn(Optional.of(store));
            given(storeRepository.existsByStoreName(NEW_STORE_NAME)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> adminStoreService.updateStoreWithName(store.getId(), request)
            ).isInstanceOf(BbangleException.class);

            verify(adminStoreMapper, never()).toStoreDetailResponse(any());
        }
    }
}