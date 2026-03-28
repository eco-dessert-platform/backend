package com.bbangle.bbangle.seller.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication;
import com.bbangle.bbangle.store.domain.model.StoreApprovalStatus;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위 테스트] AdminSellerService")
@ExtendWith(MockitoExtension.class)
class AdminSellerServiceUnitTest {

    @InjectMocks
    private AdminSellerService adminSellerService;

    @Mock
    private StoreApplicationRepository storeApplicationRepository;

    @Nested
    @DisplayName("getAdminSellerApplicationList() 테스트")
    class GetAdminSellerApplicationListTest {

        private final int DEFAULT_PAGE_SIZE = 100;

        @Test
        @DisplayName("판매자의 스토어 등록 신청 목록을 조회한다. - 첫 페이지 조회")
        void success_getAdminSellerApplicationList_firstPage() {

            // given
            int page = 1;

            List<AdminSellerApplication> mockContent = List.of(
                mock(AdminSellerApplication.class),
                mock(AdminSellerApplication.class)
            );

            given(storeApplicationRepository.findSellerApplications(0, DEFAULT_PAGE_SIZE)).willReturn(mockContent);
            given(storeApplicationRepository.countByStatus(StoreApprovalStatus.PENDING)).willReturn(250L);

            // when
            AdminSellerResponse.AdminSellerApplicationList result = adminSellerService.getAdminSellerApplicationList(page);

            // then
            assertThat(result.adminSellerApplicationList()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(250);
            assertThat(result.totalPages()).isEqualTo(3);

            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.hasNext()).isTrue();

            verify(storeApplicationRepository).findSellerApplications(0, DEFAULT_PAGE_SIZE);
            verify(storeApplicationRepository).countByStatus(StoreApprovalStatus.PENDING);
        }

        @Test
        @DisplayName("판매자의 스토어 등록 신청 목록을 조회한다. - 중간 페이지 조회")
        void success_getAdminSellerApplicationList_middlePage() {

            // given
            int page = 2;

            List<AdminSellerApplication> mockContent = List.of(mock(AdminSellerApplication.class));

            given(storeApplicationRepository.findSellerApplications(100, DEFAULT_PAGE_SIZE)).willReturn(mockContent);
            given(storeApplicationRepository.countByStatus(StoreApprovalStatus.PENDING)).willReturn(250L);

            // when
            AdminSellerResponse.AdminSellerApplicationList result = adminSellerService.getAdminSellerApplicationList(page);

            // then
            assertThat(result.totalPages()).isEqualTo(3);
            assertThat(result.hasPrevious()).isTrue();
            assertThat(result.hasNext()).isTrue();

            verify(storeApplicationRepository).findSellerApplications(100, DEFAULT_PAGE_SIZE);
            verify(storeApplicationRepository).countByStatus(StoreApprovalStatus.PENDING);
        }

        @Test
        @DisplayName("판매자의 스토어 등록 신청 목록을 조회한다. - 마지막 페이지 조회")
        void success_getAdminSellerApplicationList_lastPage() {

            // given
            int page = 3;

            given(storeApplicationRepository.findSellerApplications(200, DEFAULT_PAGE_SIZE)).willReturn(List.of());
            given(storeApplicationRepository.countByStatus(StoreApprovalStatus.PENDING)).willReturn(250L);

            // when
            AdminSellerResponse.AdminSellerApplicationList result = adminSellerService.getAdminSellerApplicationList(page);

            // then
            assertThat(result.totalPages()).isEqualTo(3);
            assertThat(result.hasPrevious()).isTrue();
            assertThat(result.hasNext()).isFalse();

            verify(storeApplicationRepository).findSellerApplications(200, DEFAULT_PAGE_SIZE);
            verify(storeApplicationRepository).countByStatus(StoreApprovalStatus.PENDING);
        }

        @Test
        @DisplayName("데이터 갯수가 페이지 크기와 동일한 경우 페이지 1로 표시한다.")
        void success_getAdminSellerApplicationList_sameSize() {

            // given
            int page = 1;
            List<AdminSellerApplication> mockContent = IntStream.range(0, 100)
                .mapToObj(i -> mock(AdminSellerApplication.class))
                .toList();

            given(storeApplicationRepository.findSellerApplications(0, DEFAULT_PAGE_SIZE)).willReturn(mockContent);
            given(storeApplicationRepository.countByStatus(StoreApprovalStatus.PENDING)).willReturn(100L); // 딱 1페이지

            // when
            AdminSellerResponse.AdminSellerApplicationList result = adminSellerService.getAdminSellerApplicationList(page);

            // then
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.hasNext()).isFalse();

            verify(storeApplicationRepository).countByStatus(StoreApprovalStatus.PENDING);
        }

        @Test
        @DisplayName("데이터가 없는 경우 빈 리스트를 반환한다.")
        void getAdminSellerApplicationList_empty() {

            // given
            int page = 1;

            given(storeApplicationRepository.findSellerApplications(0, DEFAULT_PAGE_SIZE)).willReturn(List.of());
            given(storeApplicationRepository.countByStatus(StoreApprovalStatus.PENDING)).willReturn(0L);

            // when
            AdminSellerResponse.AdminSellerApplicationList result = adminSellerService.getAdminSellerApplicationList(page);

            // then
            assertThat(result.adminSellerApplicationList()).isEmpty();
            assertThat(result.totalPages()).isEqualTo(0);
            assertThat(result.totalElements()).isEqualTo(0);
            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.hasNext()).isFalse();

            verify(storeApplicationRepository).countByStatus(StoreApprovalStatus.PENDING);
        }
    }
}