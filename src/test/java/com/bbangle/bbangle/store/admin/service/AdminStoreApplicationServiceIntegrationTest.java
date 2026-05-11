package com.bbangle.bbangle.store.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.StoreApplication;
import com.bbangle.bbangle.store.repository.StoreApplicationRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] AdminStoreApplicationService")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminStoreApplicationServiceIntegrationTest {

    @Autowired
    private AdminStoreApplicationService adminStoreApplicationService;

    @Autowired
    private StoreApplicationRepository storeApplicationRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Test
    @DisplayName("ID 목록으로 StoreApplication을 조회한다")
    void findAllByIds_success() {

        // given
        Seller seller1 = sellerRepository.save(SellerFixture.defaultSeller());
        StoreApplication app1 = storeApplicationRepository.save(
            StoreApplicationFixture.defaultStoreApplication(seller1, null)
        );
        Seller seller2 = sellerRepository.save(SellerFixture.defaultSeller());
        StoreApplication app2 = storeApplicationRepository.save(
            StoreApplicationFixture.defaultStoreApplication(seller2, null)
        );

        List<Long> ids = List.of(app1.getId(), app2.getId());

        // when
        List<StoreApplication> result = adminStoreApplicationService.findAllByIds(ids);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
            .extracting(StoreApplication::getId)
            .containsExactlyInAnyOrder(app1.getId(), app2.getId());
    }

    @Test
    @DisplayName("일부 ID만 존재하면 존재하는 데이터만 반환한다")
    void findAllByIds_partial_exist() {

        // given
        Seller seller = sellerRepository.save(SellerFixture.defaultSeller());
        StoreApplication app = storeApplicationRepository.save(
            StoreApplicationFixture.defaultStoreApplication(seller, null)
        );
        List<Long> ids = List.of(app.getId(), 999L);

        // when
        List<StoreApplication> result = adminStoreApplicationService.findAllByIds(ids);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(app.getId());
    }

    @Test
    @DisplayName("빈 ID 리스트를 전달하면 빈 리스트를 반환한다")
    void findAllByIds_empty_list() {

        // given
        List<Long> ids = List.of();

        // when
        List<StoreApplication> result = adminStoreApplicationService.findAllByIds(ids);

        // then
        assertThat(result).isEmpty();
    }
}