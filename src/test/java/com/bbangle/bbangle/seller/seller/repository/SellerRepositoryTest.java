package com.bbangle.bbangle.seller.seller.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[슬라이스 테스트] SellerRepository")
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
class SellerRepositoryTest {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    private Store store;
    private Seller seller;

    @BeforeEach
    void setUp() {
        store = storeRepository.save(StoreFixture.defaultStore());
        seller = sellerRepository.save(SellerFixture.defaultSeller(store));

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("existsByIdAndStore_IdAndIsDeletedFalse() 테스트")
    class ExistsByIdAndStoreIdAndIsDeletedFalseTest {

        @Test
        @DisplayName("sellerId가 storeId를 소유한 활성 판매자이면 true를 반환한다.")
        void true_when_ownerMatches() {

            // when
            Boolean result = sellerRepository.existsByIdAndStore_IdAndIsDeletedFalse(seller.getId(), store.getId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("sellerId는 존재하지만 storeId가 다르면 false를 반환한다.")
        void false_when_storeIdMismatch() {

            // given
            Store anotherStore = storeRepository.save(StoreFixture.defaultStore("다른스토어"));

            em.flush();
            em.clear();

            // when
            Boolean result = sellerRepository.existsByIdAndStore_IdAndIsDeletedFalse(seller.getId(), anotherStore.getId());

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("storeId는 일치하지만 다른 sellerId면 false를 반환한다.")
        void false_when_sellerIdMismatch() {

            // given
            long invalidSellerId = 999L;

            // when
            Boolean result = sellerRepository.existsByIdAndStore_IdAndIsDeletedFalse(invalidSellerId, store.getId());

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("판매자가 삭제된 상태이면 storeId가 일치해도 false를 반환한다.")
        void false_when_sellerDeleted() {

            // given
            seller.delete();
            sellerRepository.save(seller);

            em.flush();
            em.clear();

            // when
            Boolean result = sellerRepository.existsByIdAndStore_IdAndIsDeletedFalse(seller.getId(), store.getId());

            // then
            assertThat(result).isFalse();
        }
    }
}
