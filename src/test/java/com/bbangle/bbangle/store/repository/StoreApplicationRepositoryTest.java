package com.bbangle.bbangle.store.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreApplicationFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.StoreApplication;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[슬라이스 테스트] StoreApplicationRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class StoreApplicationRepositoryTest {

    @Autowired
    private StoreApplicationRepository repository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("판매자가 신청한 스토어 등록 정보 중 제일 최근 1개만 조회한다.")
    void success_findLatestBySellerId() {

        // given
        Seller seller = sellerRepository.saveAndFlush(SellerFixture.defaultSeller());
        Store store = storeRepository.saveAndFlush(StoreFixture.defaultStore());

        StoreApplication storeApplication1 = repository
            .saveAndFlush(StoreApplicationFixture.defaultStoreApplication(seller, store));
        StoreApplication storeApplication2 = repository
            .saveAndFlush(StoreApplicationFixture.defaultStoreApplication(seller, store));

        em.createQuery("""
                          update StoreApplication s
                          set s.createdAt = :time
                          where s.id = :id
                          """)
            .setParameter("time", LocalDateTime.of(2026,1,1,0,0))
            .setParameter("id", storeApplication1.getId())
            .executeUpdate();

        em.createQuery("""
                          update StoreApplication s
                          set s.createdAt = :time
                          where s.id = :id
                          """)
            .setParameter("time", LocalDateTime.of(2026,2,1,0,0))
            .setParameter("id", storeApplication2.getId())
            .executeUpdate();

        em.clear();

        // when
        Optional<StoreApplication> result = repository.findLatestBySellerId(seller.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(storeApplication2.getId());
    }
}