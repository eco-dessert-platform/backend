package com.bbangle.bbangle.seller.admin.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse;
import com.bbangle.bbangle.seller.admin.controller.dto.AdminSellerResponse.AdminSellerApplication;
import com.bbangle.bbangle.seller.admin.service.AdminSellerService;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo;
import com.bbangle.bbangle.seller.admin.service.model.AdminSellerInfo.SellerApplicationInfoList.SellerApplicationInfo;
import com.bbangle.bbangle.util.AesEncryptionUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위 테스트] AdminSellerFacade")
@ExtendWith(MockitoExtension.class)
class AdminSellerFacadeUnitTest {

    @Mock
    private AdminSellerService adminSellerService;

    @Mock
    private AesEncryptionUtil aesEncryptionUtil;

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
}