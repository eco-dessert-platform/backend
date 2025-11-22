package com.bbangle.bbangle.seller.seller.facade;


import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.config.S3IntegrationTestSupport;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerFacade")
@Transactional
public class SellerFacadeTest extends S3IntegrationTestSupport {

    @Autowired
    private SellerFacade sellerFacade;
    @Autowired
    private SellerRepository sellerRepository;
    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("유효한 정보가 주어지면 판매자 등록에 성공한다 - 스토어 아이디가 없는 경우.")
    void success_registerSeller_withOut_storeId() {
        // arrange
        Long storeId = null;
        SellerCreateCommand command = SellerCreateCommand.builder()
            .storeName("빵그리 상점1123")
            .phoneNumber("01012345678")
            .subPhoneNumber("01012345678")
            .email("test@gmail.com")
            .originAddress("경기도 수원시 팔달구")
            .originAddressDetail("화성행궁 12번지")
            .storeId(storeId)
            .build();
        MockMultipartFile mockFile = new MockMultipartFile(
            "profileImage",             // 파라미터 이름 (컨트롤러에서 받는 이름)
            "test-image.png",           // 파일명
            "image/png",                // Content-Type
            "fake image content".getBytes()  // 파일 내용
        );
        // act
        sellerFacade.registerSeller(command, mockFile, command.storeId());

        // assert
        Seller savedSeller = sellerRepository.findAll().get(0);

        Store savedStore = savedSeller.getStore();

        assertThat(savedSeller).isNotNull();
        assertThat(savedStore).isNotNull();

        assertThat(savedStore.getName()).isEqualTo(command.storeName());
        assertThat(savedSeller.getPhoneNumberVO().getPhoneNumber()).isEqualTo(command.phoneNumber());
        assertThat(savedSeller.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(command.subPhoneNumber());
        assertThat(savedSeller.getEmailVO().getEmail()).isEqualTo(command.email());
        assertThat(savedSeller.getOriginAddressLine()).isEqualTo(command.originAddress());
        assertThat(savedSeller.getOriginAddressDetail()).isEqualTo(command.originAddressDetail());
        assertThat(savedSeller.getProfile()).isNotBlank();
    }


    @Test
    @DisplayName("유효한 정보가 주어지면 판매자 등록에 성공한다 - 스토어 아이디가 존재하는 경우.")
    void success_registerSeller_existed_storeId() {
        // arrange
        Store registStore = storeRepository.saveAndFlush(Store.createForSeller("기존 상점"));

        SellerCreateCommand command = SellerCreateCommand.builder()
            .storeName("빵그리 상점1123")
            .phoneNumber("01012345678")
            .subPhoneNumber("01012345678")
            .email("test@gmail.com")
            .originAddress("경기도 수원시 팔달구")
            .originAddressDetail("화성행궁 12번지")
            .storeId(registStore.getId())
            .build();
        MockMultipartFile mockFile = new MockMultipartFile(
            "profileImage",             // 파라미터 이름 (컨트롤러에서 받는 이름)
            "test-image.png",           // 파일명
            "image/png",                // Content-Type
            "fake image content".getBytes()  // 파일 내용
        );
        // act
        sellerFacade.registerSeller(command, mockFile, command.storeId());

        // assert
        Seller savedSeller = sellerRepository.findAll().get(0);

        Store findStore = savedSeller.getStore();

        assertThat(savedSeller).isNotNull();
        assertThat(findStore).isNotNull();

        assertThat(findStore.getName()).isEqualTo(registStore.getName());
        assertThat(savedSeller.getPhoneNumberVO().getPhoneNumber()).isEqualTo(command.phoneNumber());
        assertThat(savedSeller.getPhoneNumberVO().getSubPhoneNumber()).isEqualTo(command.subPhoneNumber());
        assertThat(savedSeller.getEmailVO().getEmail()).isEqualTo(command.email());
        assertThat(savedSeller.getOriginAddressLine()).isEqualTo(command.originAddress());
        assertThat(savedSeller.getOriginAddressDetail()).isEqualTo(command.originAddressDetail());
        assertThat(savedSeller.getProfile()).isNotBlank();
    }

}
