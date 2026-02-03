package com.bbangle.bbangle.claim.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.claim.domain.ReturnRequest;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.claim.ReturnRequestFixture;
import com.bbangle.bbangle.fixture.order.OrderItemFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
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

@DisplayName("[Repository] ClaimRepository")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClaimRepositoryDataJpaTest {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Nested
    @DisplayName("existsReturnRequestBySeller")
    class ExistsReturnRequestBySellerTests {

        private Store store;
        private Seller seller;
        private Product product;
        private OrderItem orderItem;
        private ReturnRequest returnRequest;

        @BeforeEach
        void setUp() {
            // Store → Product → OrderItem → ReturnRequest, Store ← Seller
            store = storeRepository.save(StoreFixture.defaultStore());
            seller = sellerRepository.save(SellerFixture.defaultSeller(store));
            product = productRepository.save(ProductFixture.defaultProductWithStore(store));
            orderItem = orderItemRepository.save(OrderItemFixture.returnRequestedWithProduct(product));
            returnRequest = claimRepository.save(ReturnRequestFixture.requested(orderItem));

            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("판매자가 반품을 소유하면 true를 반환한다")
        void given_sellerOwnsReturn_when_exists_then_returnTrue() {
            // when
            boolean result = claimRepository.existsClaimRequestBySeller(returnRequest.getId(), seller.getId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("다른 판매자는 false를 반환한다")
        void given_differentSeller_when_exists_then_returnFalse() {
            // given
            Store otherStore = storeRepository.save(StoreFixture.defaultStore());
            Seller otherSeller = sellerRepository.save(SellerFixture.defaultSeller(otherStore));
            em.flush();
            em.clear();

            // when
            boolean result = claimRepository.existsClaimRequestBySeller(returnRequest.getId(), otherSeller.getId());

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 반품이면 false를 반환한다")
        void given_nonExistentReturn_when_exists_then_returnFalse() {
            // given
            Long nonExistentReturnId = 99999L;

            // when
            boolean result = claimRepository.existsClaimRequestBySeller(nonExistentReturnId, seller.getId());

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("여러 판매자 환경에서 각 판매자의 반품만 true를 반환한다")
        void given_multipleSellersWithReturns_when_exists_then_returnCorrectResults() {
            // given
            Store storeB = storeRepository.save(StoreFixture.defaultStore());
            Seller sellerB = sellerRepository.save(SellerFixture.defaultSeller(storeB));

            Product productB = productRepository.save(ProductFixture.defaultProductWithStore(storeB));
            OrderItem orderItemB = orderItemRepository.save(OrderItemFixture.returnRequestedWithProduct(productB));
            ReturnRequest returnRequestB = claimRepository.save(ReturnRequestFixture.requested(orderItemB));

            em.flush();
            em.clear();

            // when & then
            assertThat(claimRepository.existsClaimRequestBySeller(returnRequest.getId(), seller.getId()))
                .isTrue();
            assertThat(claimRepository.existsClaimRequestBySeller(returnRequest.getId(), sellerB.getId()))
                .isFalse();
            assertThat(claimRepository.existsClaimRequestBySeller(returnRequestB.getId(), seller.getId()))
                .isFalse();
            assertThat(claimRepository.existsClaimRequestBySeller(returnRequestB.getId(), sellerB.getId()))
                .isTrue();
        }

        @Test
        @DisplayName("같은 store의 다른 seller는 false를 반환한다")
        void given_samestoreWithDifferentSeller_when_exists_then_returnFalse() {
            // given
            Store otherStore = storeRepository.save(StoreFixture.defaultStore());
            Seller anotherSeller = sellerRepository.save(SellerFixture.defaultSeller(otherStore));
            em.flush();
            em.clear();

            // when
            boolean result = claimRepository.existsClaimRequestBySeller(returnRequest.getId(), anotherSeller.getId());

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("존재하지 않는 seller id로 조회하면 false를 반환한다")
        void given_nonExistentSellerId_when_exists_then_returnFalse() {
            // given
            Long nonExistentSellerId = 99999L;

            // when
            boolean result = claimRepository.existsClaimRequestBySeller(returnRequest.getId(), nonExistentSellerId);

            // then
            assertThat(result).isFalse();
        }
    }
}
