package com.bbangle.bbangle.seller.seller.service;

import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.domain.model.CertificationStatus;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.seller.seller.service.command.SellerCreateCommand;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("[통합테스트] SellerIntegrationTest")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SellerServiceTest {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;


    @Test
    @DisplayName("신규 스토어와 함께 판매자를 생성한다")
    void createSeller_withNewStore() {
        // given
        SellerCreateCommand command = new SellerCreateCommand("빵그리 상점1123", "01012345678",
            "01012345678", "test@gmail.com", "경기도 수원시 팔달구", "화성행궁 12번지");
        String profileImagePath = "path/to/image.jpg";
        Long storeId = null;

        // when
        sellerService.createSeller(command, profileImagePath, storeId);
        em.flush();
        em.clear();

        // then
        Seller savedSeller = sellerRepository.findAll().get(0);
        Store savedStore = storeRepository.findAll().get(0);

        assertThat(savedSeller).isNotNull();
        assertThat(savedSeller.getPhone()).isEqualTo(command.phoneNumber());
        assertThat(savedSeller.getProfile()).isEqualTo(profileImagePath);
        assertThat(savedSeller.getCertificationStatus()).isEqualTo(CertificationStatus.PENDING);
        assertThat(savedStore).isNotNull();
        assertThat(savedStore.getName()).isEqualTo(command.storeName());
        assertThat(savedSeller.getStore()).isEqualTo(savedStore);
    }

    @Test
    @DisplayName("기존 스토어에 판매자를 연결하여 생성한다")
    void createSeller_withExistingStore() {
        // given
        Store existingStore = storeRepository.save(Store.builder().name("기존 스토어").build());
        Long storeId = existingStore.getId();
        SellerCreateCommand command = new SellerCreateCommand("빵그리 상점1123", "01012345678",
            "01012345678", "test@gmail.com", "경기도 수원시 팔달구", "화성행궁 12번지");
        String profileImagePath = "path/to/image.jpg";

        // when
        sellerService.createSeller(command, profileImagePath, storeId);
        em.flush();
        em.clear();

        // then
        Seller savedSeller = sellerRepository.findAll().get(0);

        assertThat(savedSeller).isNotNull();
        assertThat(savedSeller.getPhone()).isEqualTo(command.phoneNumber());
        assertThat(savedSeller.getStore().getId()).isEqualTo(storeId);
        assertThat(savedSeller.getStore().getName()).isEqualTo(
            "기존 스토어"); // command의 storeName이 아닌 기존 스토어 이름을 따라야 함
    }

}
