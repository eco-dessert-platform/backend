package com.bbangle.bbangle.seller.admin.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerRequest.StoreApplicationApprove;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationApproveList;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationApproveList.SuccessDetail;
import com.bbangle.bbangle.seller.admin.controller.mapper.AdminSellerMapper;
import com.bbangle.bbangle.seller.admin.service.AdminSellerService;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.admin.service.AdminStoreApplicationService;
import com.bbangle.bbangle.store.admin.service.AdminStoreService;
import com.bbangle.bbangle.store.admin.service.model.RegisterApproveResult;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.util.AesEncryptionUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@DisplayName("[단위 테스트] AdminSellerFacade")
@ExtendWith(MockitoExtension.class)
class AdminSellerFacadeUnitTest {

    @Mock
    private AdminSellerService adminSellerService;

    @Mock
    private AesEncryptionUtil aesEncryptionUtil;

    @Mock
    private AdminStoreApplicationService adminStoreApplicationService;

    @Mock
    private AdminStoreService adminStoreService;

    @Mock
    private AdminSellerMapper adminSellerMapper;

    @InjectMocks
    private AdminSellerFacade adminSellerFacade;

    @Nested
    @DisplayName("getAdminSellerApplicationList() 테스트")
    class GetAdminSellerApplicationListTest {

        @Test
        @DisplayName("판매자 스토어 등록 신청 목록을 조회한다.")
        void success_getAdminSellerApplicationList() {

            // given
            int page = 1;

            String encryptedAccountNumber = "encrypted";
            String decryptedAccountNumber = "123-456-789";

            AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo rawItem =
                new SellerApplicationInfo(
                    1L,
                    AdminSellerInfo.SellerStoreInfo.builder()
                        .storeName("테스트 상점")
                        .phone("010-1111-2222")
                        .subPhone("010-3333-4444")
                        .email("test@test.com")
                        .originAddressLine("서울")
                        .originAddressDetail("상세주소")
                        .build(),
                    AdminSellerInfo.SellerInfo.builder()
                        .sellerId(10L)
                        .bankCode("KB")
                        .accountHolder("홍길동")
                        .accountNumber(encryptedAccountNumber)
                        .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                        .build()
                );

            AdminSellerInfo.SellerApplicationInfoList serviceResult =
                AdminSellerInfo.SellerApplicationInfoList.builder()
                    .sellerApplicationInfoList(List.of(rawItem))
                    .totalElements(1L)
                    .totalPages(1)
                    .hasPrevious(false)
                    .hasNext(false)
                    .build();

            given(adminSellerService.getAdminSellerApplicationList(page)).willReturn(serviceResult);
            given(aesEncryptionUtil.decrypt(encryptedAccountNumber)).willReturn(decryptedAccountNumber);

            // when
            AdminSellerResponse.AdminSellerApplicationList result = adminSellerFacade.getAdminSellerApplicationList(page);

            // then
            assertThat(result).isNotNull();
            assertThat(result.adminSellerApplicationList()).hasSize(1);

            AdminSellerApplication response = result.adminSellerApplicationList().get(0);

            // 🔥 핵심 검증: 복호화 + 매핑
            assertThat(response.sellerDTO().accountNumber()).isEqualTo(decryptedAccountNumber);
            assertThat(response.sellerDTO().sellerId()).isEqualTo(10L);
            assertThat(response.sellerDTO().bankCode()).isEqualTo("KB");
            assertThat(response.sellerStoreDTO().storeName()).isEqualTo("테스트 상점");
            assertThat(result.totalElements()).isEqualTo(1L);

            // 🔥 interaction 검증
            verify(adminSellerService, times(1)).getAdminSellerApplicationList(page);
            verify(aesEncryptionUtil, times(1)).decrypt(encryptedAccountNumber);
        }

        @Test
        @DisplayName("조회 결과가 없을 때 빈 리스트를 반환한다.")
        void getAdminSellerApplicationList_empty() {

            // given
            given(adminSellerService.getAdminSellerApplicationList(1))
                .willReturn(
                    AdminSellerInfo.SellerApplicationInfoList.builder()
                        .sellerApplicationInfoList(List.of())
                        .totalElements(0L)
                        .totalPages(0)
                        .hasPrevious(false)
                        .hasNext(false)
                        .build()
                );

            // when
            AdminSellerResponse.AdminSellerApplicationList result = adminSellerFacade.getAdminSellerApplicationList(1);

            // then
            assertThat(result.adminSellerApplicationList()).isEmpty();
            verify(adminSellerService, times(1)).getAdminSellerApplicationList(1);
            verify(aesEncryptionUtil, never()).decrypt(any());
        }
    }

    @Nested
    @DisplayName("approveStoreApplications() 테스트")
    class ApproveStoreApplicationsTest {

        @Test
        @DisplayName("스토어 신청 승인에 성공한다.")
        void approveStoreApplications_success() {

            // given
            Long applicationId = 1L;

            StoreApplicationApprove request = mock(StoreApplicationApprove.class);
            StoreApplication storeApplication = mock(StoreApplication.class);
            Store store = mock(Store.class);
            Seller seller = mock(Seller.class);
            SuccessDetail.StoreDTO storeDto = mock(SuccessDetail.StoreDTO.class);
            SuccessDetail.SellerDTO sellerDto = mock(SuccessDetail.SellerDTO.class);
            RegisterApproveResult result = RegisterApproveResult.builder()
                .storeApplication(storeApplication)
                .store(store)
                .seller(seller)
                .build();

            given(request.applicationId()).willReturn(applicationId);
            given(storeApplication.getId()).willReturn(applicationId);
            given(storeApplication.getStatus()).willReturn(StoreApprovalStatus.APPROVE);
            given(adminStoreApplicationService.findAllByIds(List.of(applicationId))).willReturn(List.of(storeApplication));
            given(adminStoreService.registerApprove(applicationId, request)).willReturn(result);
            given(adminSellerMapper.toApproveStoreDto(store)).willReturn(storeDto);
            given(adminSellerMapper.toApproveSellerDto(seller)).willReturn(sellerDto);

            // when
            AdminSellerApplicationApproveList response = adminSellerFacade.approveStoreApplications(List.of(request));

            // then
            assertThat(response.successDetails()).hasSize(1);
            assertThat(response.failDetails()).isEmpty();

            AdminSellerApplicationApproveList.SuccessDetail successDetail = response.successDetails().get(0);

            assertThat(successDetail.storeApplicationId()).isEqualTo(applicationId);
            assertThat(successDetail.storeApplicationStatus()).isEqualTo(StoreApprovalStatus.APPROVE);
            assertThat(successDetail.storeDTO()).isEqualTo(storeDto);
            assertThat(successDetail.sellerDTO()).isEqualTo(sellerDto);

            verify(storeApplication).validateApprovable();
            verify(adminStoreService).registerApprove(applicationId, request);
            verify(adminSellerMapper).toApproveStoreDto(store);
            verify(adminSellerMapper).toApproveSellerDto(seller);
        }

        @Test
        @DisplayName("스토어 신청이 존재하지 않으면 실패를 반환한다.")
        void approveStoreApplications_notFound() {

            // given
            Long applicationId = 1L;
            StoreApplicationApprove request = mock(StoreApplicationApprove.class);

            given(request.applicationId()).willReturn(applicationId);
            given(adminStoreApplicationService.findAllByIds(List.of(applicationId))).willReturn(List.of());

            // when
            AdminSellerApplicationApproveList response = adminSellerFacade.approveStoreApplications(List.of(request));

            // then
            assertThat(response.successDetails()).isEmpty();
            assertThat(response.failDetails()).hasSize(1);

            AdminSellerResponse.FailDetail failDetail = response.failDetails().get(0);

            assertThat(failDetail.storeApplicationId()).isEqualTo(applicationId);
            assertThat(failDetail.reason()).isEqualTo(BbangleErrorCode.NOT_FOUND_REQUEST.getMessage());

            verify(adminStoreService, never()).registerApprove(anyLong(), any());
        }

        @Test
        @DisplayName("중복 승인 시 Unique 충돌 발생하면 실패 처리한다.")
        void approveStoreApplications_duplicateStore() {

            // given
            Long applicationId = 1L;

            StoreApplicationApprove request = mock(StoreApplicationApprove.class);
            StoreApplication storeApplication = mock(StoreApplication.class);

            given(request.applicationId()).willReturn(applicationId);
            given(storeApplication.getId()).willReturn(applicationId);
            given(adminStoreApplicationService.findAllByIds(List.of(applicationId))).willReturn(List.of(storeApplication));
            given(adminStoreService.registerApprove(applicationId, request)).willThrow(new DataIntegrityViolationException("duplicate"));

            // when
            AdminSellerApplicationApproveList response = adminSellerFacade.approveStoreApplications(List.of(request));

            // then
            assertThat(response.successDetails()).isEmpty();
            assertThat(response.failDetails()).hasSize(1);

            AdminSellerResponse.FailDetail failDetail = response.failDetails().get(0);

            assertThat(failDetail.storeApplicationId()).isEqualTo(applicationId);
            assertThat(failDetail.reason()).isEqualTo(BbangleErrorCode.ALREADY_RESERVED_STORE.getMessage());
        }

        @Test
        @DisplayName("비즈니스 예외 발생 시 실패 처리한다.")
        void approveStoreApplications_bbangleException() {

            // given
            Long applicationId = 1L;
            StoreApplicationApprove request = mock(StoreApplicationApprove.class);
            StoreApplication storeApplication = mock(StoreApplication.class);

            given(request.applicationId()).willReturn(applicationId);
            given(storeApplication.getId()).willReturn(applicationId);
            given(adminStoreApplicationService.findAllByIds(List.of(applicationId))).willReturn(List.of(storeApplication));
            willThrow(new BbangleException(BbangleErrorCode.ALREADY_REGISTER_STORE)).given(storeApplication).validateApprovable();

            // when
            AdminSellerApplicationApproveList response = adminSellerFacade.approveStoreApplications(List.of(request));

            // then
            assertThat(response.successDetails()).isEmpty();
            assertThat(response.failDetails()).hasSize(1);

            AdminSellerResponse.FailDetail failDetail = response.failDetails().get(0);

            assertThat(failDetail.storeApplicationId()).isEqualTo(applicationId);
            assertThat(failDetail.reason()).isEqualTo(BbangleErrorCode.ALREADY_REGISTER_STORE.getMessage());
            verify(adminStoreService, never()).registerApprove(anyLong(), any());
        }

        @Test
        @DisplayName("여러 요청 중 부분 성공과 부분 실패 처리를 한다.")
        void approveStoreApplications_partialSuccess() {

            // given
            Long successId = 1L;
            Long failId = 2L;

            StoreApplicationApprove successRequest = mock(StoreApplicationApprove.class);
            StoreApplicationApprove failRequest = mock(StoreApplicationApprove.class);
            StoreApplication successApplication = mock(StoreApplication.class);
            Store store = mock(Store.class);
            Seller seller = mock(Seller.class);
            SuccessDetail.StoreDTO storeDto = mock(SuccessDetail.StoreDTO.class);
            SuccessDetail.SellerDTO sellerDto = mock(SuccessDetail.SellerDTO.class);

            given(successRequest.applicationId()).willReturn(successId);
            given(failRequest.applicationId()).willReturn(failId);
            given(successApplication.getId()).willReturn(successId);
            given(successApplication.getStatus()).willReturn(StoreApprovalStatus.APPROVE);
            given(adminStoreApplicationService.findAllByIds(List.of(successId, failId))).willReturn(List.of(successApplication));
            given(adminStoreService.registerApprove(successId, successRequest))
                .willReturn(RegisterApproveResult.builder()
                    .storeApplication(successApplication)
                    .seller(seller)
                    .store(store)
                    .build()
                );
            given(adminSellerMapper.toApproveStoreDto(store)).willReturn(storeDto);
            given(adminSellerMapper.toApproveSellerDto(seller)).willReturn(sellerDto);

            // when
            AdminSellerApplicationApproveList response =adminSellerFacade.approveStoreApplications(List.of(successRequest, failRequest));

            // then
            assertThat(response.successDetails()).hasSize(1);
            assertThat(response.failDetails()).hasSize(1);
            assertThat(response.successDetails().get(0).storeApplicationId()).isEqualTo(successId);
            assertThat(response.failDetails().get(0).storeApplicationId()).isEqualTo(failId);
            assertThat(response.successDetails().get(0).storeDTO()).isEqualTo(storeDto);
            assertThat(response.successDetails().get(0).sellerDTO()).isEqualTo(sellerDto);
        }

        @Test
        @DisplayName("조회 결과 순서와 관계없이 요청 순서대로 처리한다")
        void approveStoreApplications_orderPreserved() {

            // given
            Long firstId = 1L;
            Long secondId = 2L;

            StoreApplicationApprove firstRequest = mock(StoreApplicationApprove.class);
            StoreApplicationApprove secondRequest = mock(StoreApplicationApprove.class);

            StoreApplication firstApplication = mock(StoreApplication.class);
            StoreApplication secondApplication = mock(StoreApplication.class);

            RegisterApproveResult firstResult = RegisterApproveResult.builder()
                .storeApplication(firstApplication)
                .store(mock(Store.class))
                .seller(mock(Seller.class))
                .build();

            RegisterApproveResult secondResult = RegisterApproveResult.builder()
                .storeApplication(secondApplication)
                .store(mock(Store.class))
                .seller(mock(Seller.class))
                .build();

            given(firstRequest.applicationId()).willReturn(firstId);
            given(secondRequest.applicationId()).willReturn(secondId);

            given(firstApplication.getId()).willReturn(firstId);
            given(secondApplication.getId()).willReturn(secondId);

            // DB 조회 순서 뒤집기
            given(adminStoreApplicationService.findAllByIds(List.of(firstId, secondId))).willReturn(List.of(secondApplication, firstApplication));
            given(adminStoreService.registerApprove(eq(firstId), same(firstRequest))).willReturn(firstResult);
            given(adminStoreService.registerApprove(eq(secondId), same(secondRequest))).willReturn(secondResult);

            given(adminSellerMapper.toApproveStoreDto(any())).willReturn(mock(SuccessDetail.StoreDTO.class));
            given(adminSellerMapper.toApproveSellerDto(any())).willReturn(mock(SuccessDetail.SellerDTO.class));

            // when
            adminSellerFacade.approveStoreApplications(List.of(firstRequest, secondRequest));

            // then
            InOrder inOrder = inOrder(adminStoreService);
            inOrder.verify(adminStoreService).registerApprove(firstId, firstRequest);
            inOrder.verify(adminStoreService).registerApprove(secondId, secondRequest);
        }
    }
}