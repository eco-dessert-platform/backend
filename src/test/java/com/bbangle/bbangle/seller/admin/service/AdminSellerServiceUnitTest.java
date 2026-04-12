package com.bbangle.bbangle.seller.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo;
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

            List<SellerApplicationInfo> mockContent = List.of(
                mock(SellerApplicationInfo.class),
                mock(SellerApplicationInfo.class)
            );

            given(storeApplicationRepository.findSellerApplications(0, DEFAULT_PAGE_SIZE)).willReturn(mockContent);
            given(storeApplicationRepository.countSellerApplications()).willReturn(250L);

            // when
            SellerApplicationInfoList result = adminSellerService.getAdminSellerApplicationList(page);

            // then
            assertThat(result.sellerApplicationInfoList()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(250);
            assertThat(result.totalPages()).isEqualTo(3);

            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.hasNext()).isTrue();

            verify(storeApplicationRepository).findSellerApplications(0, DEFAULT_PAGE_SIZE);
            verify(storeApplicationRepository).countSellerApplications();
        }

        @Test
        @DisplayName("판매자의 스토어 등록 신청 목록을 조회한다. - 중간 페이지 조회")
        void success_getAdminSellerApplicationList_middlePage() {

            // given
            int page = 2;

            List<SellerApplicationInfo> mockContent = List.of(mock(SellerApplicationInfo.class));

            given(storeApplicationRepository.findSellerApplications(100, DEFAULT_PAGE_SIZE)).willReturn(mockContent);
            given(storeApplicationRepository.countSellerApplications()).willReturn(250L);

            // when
            SellerApplicationInfoList result = adminSellerService.getAdminSellerApplicationList(page);

            // then
            assertThat(result.totalPages()).isEqualTo(3);
            assertThat(result.hasPrevious()).isTrue();
            assertThat(result.hasNext()).isTrue();

            verify(storeApplicationRepository).findSellerApplications(100, DEFAULT_PAGE_SIZE);
            verify(storeApplicationRepository).countSellerApplications();
        }

        @Test
        @DisplayName("판매자의 스토어 등록 신청 목록을 조회한다. - 마지막 페이지 조회")
        void success_getAdminSellerApplicationList_lastPage() {

            // given
            int page = 3;

            List<SellerApplicationInfo> lastPageContent = List.of(mock(SellerApplicationInfo.class));

            given(storeApplicationRepository.findSellerApplications(200, DEFAULT_PAGE_SIZE)).willReturn(lastPageContent);
            given(storeApplicationRepository.countSellerApplications()).willReturn(250L);

            // when
            SellerApplicationInfoList result = adminSellerService.getAdminSellerApplicationList(page);

            // then
            assertThat(result.totalPages()).isEqualTo(3);
            assertThat(result.hasPrevious()).isTrue();
            assertThat(result.hasNext()).isFalse();

            verify(storeApplicationRepository).findSellerApplications(200, DEFAULT_PAGE_SIZE);
            verify(storeApplicationRepository).countSellerApplications();
        }

        @Test
        @DisplayName("데이터 갯수가 페이지 크기와 동일한 경우 페이지 1로 표시한다.")
        void success_getAdminSellerApplicationList_sameSize() {

            // given
            int page = 1;
            List<SellerApplicationInfo> mockContent = IntStream.range(0, 100)
                .mapToObj(i -> mock(SellerApplicationInfo.class))
                .toList();

            given(storeApplicationRepository.findSellerApplications(0, DEFAULT_PAGE_SIZE)).willReturn(mockContent);
            given(storeApplicationRepository.countSellerApplications()).willReturn(100L); // 딱 1페이지

            // when
            SellerApplicationInfoList result = adminSellerService.getAdminSellerApplicationList(page);

            // then
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.hasNext()).isFalse();

            verify(storeApplicationRepository).countSellerApplications();
        }

        @Test
        @DisplayName("데이터가 없는 경우 빈 리스트를 반환한다.")
        void getAdminSellerApplicationList_empty() {

            // given
            int page = 1;

            given(storeApplicationRepository.findSellerApplications(0, DEFAULT_PAGE_SIZE)).willReturn(List.of());
            given(storeApplicationRepository.countSellerApplications()).willReturn(0L);

            // when
            SellerApplicationInfoList result = adminSellerService.getAdminSellerApplicationList(page);

            // then
            assertThat(result.sellerApplicationInfoList()).isEmpty();
            assertThat(result.totalPages()).isEqualTo(0);
            assertThat(result.totalElements()).isEqualTo(0);
            assertThat(result.hasPrevious()).isFalse();
            assertThat(result.hasNext()).isFalse();

            verify(storeApplicationRepository).countSellerApplications();
        }

        @Test
        @DisplayName("요청한 페이지가 전체 페이지보다 클 경우 hasNext는 false")
        void getAdminSellerApplicationList_page_overflow() {

            // given
            int page = 5;

            given(storeApplicationRepository.findSellerApplications(400, DEFAULT_PAGE_SIZE)).willReturn(List.of());
            given(storeApplicationRepository.countSellerApplications()).willReturn(250L); // totalPages = 3

            // when
            SellerApplicationInfoList result = adminSellerService.getAdminSellerApplicationList(page);

            // then
            assertThat(result.totalPages()).isEqualTo(3);
            assertThat(result.hasPrevious()).isTrue();
            assertThat(result.hasNext()).isFalse();
        }
    }
}