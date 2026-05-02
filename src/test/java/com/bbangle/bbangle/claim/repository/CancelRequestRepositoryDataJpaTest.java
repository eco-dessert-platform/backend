package com.bbangle.bbangle.claim.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.claim.domain.CancelRequest;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.claim.CancelRequestFixture;
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
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[Repository] CancelRequestRepository")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CancelRequestRepositoryDataJpaTest {

    @Autowired
    private CancelRequestRepository sut;

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
    @DisplayName("countCancelsBySeller")
    class CountCancelsBySellerTests {

        private Store store;
        private Seller seller;
        private Product product;
        private List<CancelRequest> cancelRequests;

        @BeforeEach
        void setUp() {
            // Store → Product → OrderItem → CancelRequest, Store ← Seller
            store = storeRepository.save(StoreFixture.defaultStore());
            seller = sellerRepository.save(SellerFixture.defaultSeller(store));
            product = productRepository.save(ProductFixture.defaultProductWithStore(store));

            // 3개의 취소 요청 생성
            cancelRequests = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.cancelRequestedWithProduct(product)
                );
                CancelRequest cancelRequest = sut.save(
                    CancelRequestFixture.requested(orderItem)
                );
                cancelRequests.add(cancelRequest);
            }

            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("모든 취소 요청이 판매자 소유일 때 정확한 개수를 반환한다")
        void given_allCancelsOwnedBySeller_when_count_then_returnAllCount() {
            // given
            List<Long> cancelIds = List.of(
                cancelRequests.get(0).getId(),
                cancelRequests.get(1).getId(),
                cancelRequests.get(2).getId()
            );

            // when
            Long result = sut.countCancelsBySeller(cancelIds, seller.getId());

            // then
            assertThat(result).isEqualTo(3L);
        }

        @Test
        @DisplayName("일부만 판매자 소유일 때 올바른 개수만 카운트한다")
        void given_partialCancelsOwnedBySeller_when_count_then_returnPartialCount() {
            // given - 다른 판매자 및 상품 생성
            Store otherStore = storeRepository.save(StoreFixture.defaultStore());
            Seller otherSeller = sellerRepository.save(SellerFixture.defaultSeller(otherStore));
            Product otherProduct = productRepository.save(
                ProductFixture.defaultProductWithStore(otherStore)
            );

            // 다른 판매자의 취소 요청 추가
            OrderItem newOrderItem = orderItemRepository.save(
                OrderItemFixture.cancelRequestedWithProduct(otherProduct)
            );
            CancelRequest otherCancelRequest = sut.save(
                CancelRequestFixture.requested(newOrderItem)
            );

            List<Long> cancelIds = List.of(
                cancelRequests.get(0).getId(),
                cancelRequests.get(1).getId(),
                otherCancelRequest.getId()  // 다른 판매자의 취소
            );

            em.flush();
            em.clear();

            // when
            Long result = sut.countCancelsBySeller(cancelIds, seller.getId());

            // then - seller는 첫 두 개의 취소만 소유
            assertThat(result).isEqualTo(2L);
        }

        @Test
        @DisplayName("아무 취소 요청도 판매자 소유가 아닐 때 0을 반환한다")
        void given_noCancelsOwnedBySeller_when_count_then_returnZero() {
            // given
            Store otherStore = storeRepository.save(StoreFixture.defaultStore());
            Seller otherSeller = sellerRepository.save(SellerFixture.defaultSeller(otherStore));
            Product otherProduct = productRepository.save(
                ProductFixture.defaultProductWithStore(otherStore)
            );

            List<Long> cancelIds = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.cancelRequestedWithProduct(otherProduct)
                );
                CancelRequest cancelRequest = sut.save(
                    CancelRequestFixture.requested(orderItem)
                );
                cancelIds.add(cancelRequest.getId());
            }

            em.flush();
            em.clear();

            // when
            Long result = sut.countCancelsBySeller(cancelIds, seller.getId());

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("빈 리스트로 조회할 때 0을 반환한다")
        void given_emptyCancelIds_when_count_then_returnZero() {
            // given
            List<Long> emptyCancelIds = List.of();

            // when
            Long result = sut.countCancelsBySeller(emptyCancelIds, seller.getId());

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("존재하지 않는 취소 ID를 포함할 때 존재하는 것만 카운트한다")
        void given_nonExistentCancelIdIncluded_when_count_then_countOnlyExistent() {
            // given
            List<Long> cancelIds = List.of(
                cancelRequests.get(0).getId(),
                cancelRequests.get(1).getId(),
                99999L  // 존재하지 않는 ID
            );

            // when
            Long result = sut.countCancelsBySeller(cancelIds, seller.getId());

            // then
            assertThat(result).isEqualTo(2L);
        }

        @Test
        @DisplayName("여러 판매자 환경에서 각 판매자의 취소 요청만 정확히 카운트한다")
        void given_multipleSellers_when_count_then_returnCorrectCounts() {
            // given - 두 번째 판매자와 취소 요청 생성
            Store otherStore = storeRepository.save(StoreFixture.defaultStore());
            Seller otherSeller = sellerRepository.save(SellerFixture.defaultSeller(otherStore));
            Product otherProduct = productRepository.save(
                ProductFixture.defaultProductWithStore(otherStore)
            );

            List<CancelRequest> otherCancelRequests = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                OrderItem orderItem = orderItemRepository.save(
                    OrderItemFixture.cancelRequestedWithProduct(otherProduct)
                );
                CancelRequest cancelRequest = sut.save(
                    CancelRequestFixture.requested(orderItem)
                );
                otherCancelRequests.add(cancelRequest);
            }

            // 모든 취소 ID를 함께 조회
            List<Long> allCancelIds = new ArrayList<>();
            cancelRequests.forEach(c -> allCancelIds.add(c.getId()));
            otherCancelRequests.forEach(c -> allCancelIds.add(c.getId()));

            em.flush();
            em.clear();

            // when
            Long sellerCount = sut.countCancelsBySeller(allCancelIds, seller.getId());
            Long otherSellerCount = sut.countCancelsBySeller(
                allCancelIds,
                otherSeller.getId()
            );

            // then
            assertThat(sellerCount).isEqualTo(3L);
            assertThat(otherSellerCount).isEqualTo(2L);
        }
    }
}
