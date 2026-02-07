package com.bbangle.bbangle.store.seller.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.bbangle.bbangle.config.S3IntegrationTestSupport;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreStatus;
import com.bbangle.bbangle.store.repository.StoreRepository;
import com.bbangle.bbangle.store.seller.controller.dto.StoreRequest;
import com.bbangle.bbangle.store.seller.controller.dto.StoreResponse.StoreRegisterResult;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@DisplayName("[통합테스트] SellerStoreFacade")
@Transactional
class SellerStoreFacadeIntegrationTest extends S3IntegrationTestSupport {

    @Autowired
    private SellerStoreFacade sellerStoreFacade;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("유효한 정보가 주어지면 판매자 등록에 성공한다 - 스토어 아이디가 없는 경우.")
    void success_registerStoreForSeller_without_storeId() {

        // given
        Seller seller = SellerFixture.defaultSeller();
        sellerRepository.save(seller);

        StoreRequest.StoreCreateRequest request =
            new StoreRequest.StoreCreateRequest(
                "빵그리의 오븐",
                null,
                "비건 베이커리",
                "01012345678",
                "01098765432",
                "test@gmail.com",
                "경기도 수원시",
                "상세주소",
                null
            );

        MockMultipartFile mockFile = new MockMultipartFile(
            "profileImage",             // 파라미터 이름 (컨트롤러에서 받는 이름)
            "test-image.png",           // 파일명
            "image/png",                // Content-Type
            "fake image content".getBytes()  // 파일 내용
        );

        em.flush();
        em.clear();

        // when
        StoreRegisterResult result = sellerStoreFacade.registerStoreForSeller(seller.getId(), request, mockFile);
        em.flush();
        em.clear();

        // then
        Seller updatedSeller = sellerRepository.findById(seller.getId()).orElseThrow();
        Store store = storeRepository.findAll().get(0);

        assertThat(updatedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
        assertThat(store.getStatus()).isEqualTo(StoreStatus.RESERVED);
        assertThat(result.sellerId()).isEqualTo(seller.getId());
    }

    @Test
    @DisplayName("유효한 정보가 주어지면 판매자 등록에 성공한다 - 스토어 아이디가 존재하는 경우.")
    void success_registerStoreForSeller_exist_storeId() {

        // given
        Seller seller = SellerFixture.defaultSeller();
        sellerRepository.save(seller);

        Store store = StoreFixture.defaultStore();
        Store newStore = storeRepository.save(store);

        StoreRequest.StoreCreateRequest request =
            new StoreRequest.StoreCreateRequest(
                "빵그리의 오븐",
                newStore.getProfile(),
                "비건 베이커리",
                "01012345678",
                "01098765432",
                "test@gmail.com",
                "경기도 수원시",
                "상세주소",
                newStore.getId()
            );

        MockMultipartFile mockFile = new MockMultipartFile(
            "profileImage",             // 파라미터 이름 (컨트롤러에서 받는 이름)
            "test-image.png",           // 파일명
            "image/png",                // Content-Type
            "fake image content".getBytes()  // 파일 내용
        );

        em.flush();
        em.clear();

        // when
        StoreRegisterResult result = sellerStoreFacade.registerStoreForSeller(seller.getId(), request, mockFile);
        em.flush();
        em.clear();

        // then
        Seller updatedSeller = sellerRepository.findById(seller.getId()).orElseThrow();
        Store savedStore = storeRepository.findAll().get(0);

        assertThat(updatedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
        assertThat(savedStore.getStatus()).isEqualTo(StoreStatus.RESERVED);
        assertThat(result.sellerId()).isEqualTo(seller.getId());
    }

    @Test
    @DisplayName("이미 스토어를 등록한 판매자는 예외가 발생한다.")
    void fail_registerStoreForSeller_already_registered() {

        // given
        Seller seller = SellerFixture.defaultSeller(CertificationStatus.PENDING);
        sellerRepository.save(seller);

        StoreRequest.StoreCreateRequest request = mock(StoreRequest.StoreCreateRequest.class);

        // when & then
        assertThatThrownBy(() ->
            sellerStoreFacade.registerStoreForSeller(seller.getId(), request, mock(MultipartFile.class))
        ).isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ALREADY_REGISTER_STORE.getMessage());
    }

    @Test
    @DisplayName("이미 예약된 스토어는 등록할 수 없다.")
    void fail_registerStoreForSeller_already_reserved() {

        // given
        Seller seller = SellerFixture.defaultSeller(CertificationStatus.PENDING);
        sellerRepository.save(seller);

        Store store = StoreFixture.defaultStore();
        store.changeStatus(StoreStatus.RESERVED);
        storeRepository.save(store);

        StoreRequest.StoreCreateRequest request = mock(StoreRequest.StoreCreateRequest.class);

        MockMultipartFile mockFile = new MockMultipartFile(
            "profileImage",             // 파라미터 이름 (컨트롤러에서 받는 이름)
            "test-image.png",           // 파일명
            "image/png",                // Content-Type
            "fake image content".getBytes()  // 파일 내용
        );

        // when & then
        assertThatThrownBy(() ->
            sellerStoreFacade.registerStoreForSeller(seller.getId(), request, mockFile)
        )
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ALREADY_REGISTER_STORE.getMessage());
    }
}