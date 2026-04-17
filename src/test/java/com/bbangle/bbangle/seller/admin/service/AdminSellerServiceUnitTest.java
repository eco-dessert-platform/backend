package com.bbangle.bbangle.seller.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplicationRejectList;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo;
import com.bbangle.bbangle.store.domain.StoreApplication;
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

    @Nested
    @DisplayName("rejectStoreApplications() 테스트")
    class RejectStoreApplicationsTest {

        @Test
        @DisplayName("모든 신청이 정상적으로 거절된다")
        void success_rejectStoreApplications() {

            // given
            List<Long> ids = List.of(1L, 2L);

            StoreApplication app1 = mock(StoreApplication.class);
            StoreApplication app2 = mock(StoreApplication.class);

            given(app1.getId()).willReturn(1L);
            given(app2.getId()).willReturn(2L);
            given(storeApplicationRepository.findAllWithSellerByIdIn(ids)).willReturn(List.of(app1, app2));

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);

            // then
            assertThat(result.successIds()).containsExactlyInAnyOrder(1L, 2L);
            assertThat(result.failDetails()).isEmpty();

            verify(app1).reject();
            verify(app2).reject();
        }

        @Test
        @DisplayName("입력 순서대로 처리된다")
        void success_rejectStoreApplications_order() {

            // given
            List<Long> ids = List.of(2L, 1L);

            StoreApplication app1 = mock(StoreApplication.class);
            StoreApplication app2 = mock(StoreApplication.class);

            given(app1.getId()).willReturn(1L);
            given(app2.getId()).willReturn(2L);
            given(storeApplicationRepository.findAllWithSellerByIdIn(ids)).willReturn(List.of(app1, app2));

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);

            // then
            assertThat(result.successIds()).containsExactly(2L, 1L);
        }

        @Test
        @DisplayName("존재하지 않는 신청의 ID는 실패로 처리된다")
        void rejectStoreApplications_with_not_found() {

            // given
            List<Long> ids = List.of(1L, 2L);

            StoreApplication app1 = mock(StoreApplication.class);

            given(app1.getId()).willReturn(1L);
            given(storeApplicationRepository.findAllWithSellerByIdIn(ids)).willReturn(List.of(app1));

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);

            // then
            assertThat(result.successIds()).contains(1L);
            assertThat(result.failDetails()).hasSize(1);
            assertThat(result.failDetails().get(0).storeApplicationId()).isEqualTo(2L);
            assertThat(result.failDetails().get(0).reason()).isEqualTo(BbangleErrorCode.NOT_FOUND_REQUEST.getMessage());

            verify(app1).reject();
        }

        @Test
        @DisplayName("reject 중 비즈니스 예외 발생 시 실패로 처리된다")
        void rejectStoreApplications_business_exception() {

            // given
            List<Long> ids = List.of(1L);

            StoreApplication app = mock(StoreApplication.class);

            given(app.getId()).willReturn(1L);
            willThrow(new BbangleException(BbangleErrorCode.REQUEST_IS_APPROVED)).given(app).reject();
            given(storeApplicationRepository.findAllWithSellerByIdIn(ids)).willReturn(List.of(app));

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);

            // then
            assertThat(result.successIds()).isEmpty();
            assertThat(result.failDetails()).hasSize(1);
            assertThat(result.failDetails().get(0).reason()).isEqualTo(BbangleErrorCode.REQUEST_IS_APPROVED.getMessage());

            verify(app).reject();
        }

        @Test
        @DisplayName("알 수 없는 예외 발생 시 실패로 처리된다")
        void rejectStoreApplications_unknown_exception() {

            // given
            List<Long> ids = List.of(1L);

            StoreApplication app = mock(StoreApplication.class);

            given(app.getId()).willReturn(1L);
            willThrow(new RuntimeException()).given(app).reject();
            given(storeApplicationRepository.findAllWithSellerByIdIn(ids)).willReturn(List.of(app));

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);

            // then
            assertThat(result.failDetails()).hasSize(1);
            assertThat(result.failDetails().get(0).reason()).isEqualTo(BbangleErrorCode.INTERNAL_SERVER_ERROR.getMessage());

            verify(app).reject();
        }

        @Test
        @DisplayName("빈 id 목록이 입력되면 빈 결과를 반환한다")
        void rejectStoreApplications_empty_ids() {

            // given
            List<Long> ids = List.of();
            given(storeApplicationRepository.findAllWithSellerByIdIn(ids)).willReturn(List.of());

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);

            // then
            assertThat(result.successIds()).isEmpty();
            assertThat(result.failDetails()).isEmpty();
        }

        @Test
        @DisplayName("성공, 비즈니스 예외, 미존재가 혼합된 경우 각각 올바르게 처리된다")
        void rejectStoreApplications_mixed_result() {

            // given
            List<Long> ids = List.of(1L, 2L, 3L);

            StoreApplication app1 = mock(StoreApplication.class);
            StoreApplication app2 = mock(StoreApplication.class);

            given(app1.getId()).willReturn(1L);
            given(app2.getId()).willReturn(2L);

            willThrow(new BbangleException(BbangleErrorCode.REQUEST_IS_APPROVED)).given(app2).reject();

            // id=3L not in repository result (not found)
            given(storeApplicationRepository.findAllWithSellerByIdIn(ids)).willReturn(List.of(app1, app2));

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);

            // then
            assertThat(result.successIds()).containsExactly(1L);
            assertThat(result.failDetails()).hasSize(2);

            assertThat(result.failDetails())
                .anySatisfy(d -> {
                    assertThat(d.storeApplicationId()).isEqualTo(2L);
                    assertThat(d.reason()).isEqualTo(BbangleErrorCode.REQUEST_IS_APPROVED.getMessage());
                })
                .anySatisfy(d -> {
                    assertThat(d.storeApplicationId()).isEqualTo(3L);
                    assertThat(d.reason()).isEqualTo(BbangleErrorCode.NOT_FOUND_REQUEST.getMessage());
                });

            verify(app1).reject();
            verify(app2).reject();
        }

        @Test
        @DisplayName("모든 id가 존재하지 않으면 successIds는 비어있고 failDetails에 모두 담긴다")
        void rejectStoreApplications_all_not_found() {

            // given
            List<Long> ids = List.of(100L, 200L);
            given(storeApplicationRepository.findAllWithSellerByIdIn(ids)).willReturn(List.of());

            // when
            AdminSellerApplicationRejectList result = adminSellerService.rejectStoreApplications(ids);

            // then
            assertThat(result.successIds()).isEmpty();
            assertThat(result.failDetails()).hasSize(2);
            assertThat(result.failDetails())
                .extracting(AdminSellerApplicationRejectList.FailDetail::storeApplicationId)
                .containsExactlyInAnyOrder(100L, 200L);
            assertThat(result.failDetails())
                .extracting(AdminSellerApplicationRejectList.FailDetail::reason)
                .allMatch(r -> r.equals(BbangleErrorCode.NOT_FOUND_REQUEST.getMessage()));
        }
    }
}