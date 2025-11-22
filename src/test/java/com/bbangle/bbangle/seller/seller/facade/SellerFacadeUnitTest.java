package com.bbangle.bbangle.seller.seller.facade;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.image.customer.service.S3Service;
import com.bbangle.bbangle.seller.seller.service.SellerService;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
public class SellerFacadeUnitTest {

    @InjectMocks
    private SellerFacade sellerFacade;

    @Mock
    private SellerService sellerService;

    @Mock
    private S3Service s3Service;

    @Test
    @DisplayName("판매자 생성 중 DB 예외가 발생하면 업로드된 S3 이미지를 삭제하고 예외를 던진다")
    void registerSeller_fail_rollback_s3_image() {
        // given
        SellerCreateCommand command = SellerCreateCommand.builder()
            .storeName("빵그리 상점1123")
            .phoneNumber("01012345678")
            .subPhoneNumber("01012345678")
            .email("test@gmail.com")
            .originAddress("경기도 수원시 팔달구")
            .originAddressDetail("화성행궁 12번지")
            .storeId(1L)
            .build();

        MockMultipartFile mockFile = new MockMultipartFile("img", "test.png", "image/png",
            "content".getBytes());
        String uploadedPath = "seller-images/uuid-test.png";

        // S3 업로드는 성공한다고 가정
        given(s3Service.saveAndReturnWithCdn(anyString(), any())).willReturn(uploadedPath);

        // 핵심: DB 저장(Service) 호출 시 예외 발생을 가정
        willThrow(new BbangleException(BbangleErrorCode.SELLER_CREATION_FAILED)).given(sellerService)
            .createSeller(any(), anyString(), any());

        // when & then
        // 1. 예외가 BbangleException으로 래핑되어 던져지는지 확인
        assertThatThrownBy(() -> sellerFacade.registerSeller(command, mockFile, command.storeId()))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_CREATION_FAILED.getMessage());

        // 2. [검증] S3 삭제 메서드가 호출되었는지 확인 (보상 트랜잭션 동작 여부)
        verify(s3Service, times(1)).deleteImage(uploadedPath);
    }

}
