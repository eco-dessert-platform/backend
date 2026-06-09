package com.bbangle.bbangle.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderItemFixture;
import com.bbangle.bbangle.fixture.payment.domain.PaymentFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.Store;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DisplayName("[Repository] - OrderRepository.findByIdWithFullAssociations")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository sut;

    @Autowired
    private EntityManager em;

    private Store store;
    private Seller seller;
    private Board board;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        store = StoreFixture.defaultStore();
        em.persist(store);

        seller = SellerFixture.defaultSeller(store);
        em.persist(seller);

        board = BoardFixture.defaultBoardWithStore(store, "비건빵 모음");
        em.persist(board);

        product1 = ProductFixture.create(board, "비건 쌀빵 1종");
        product2 = ProductFixture.create(board, "비건 현미빵 1종");
        em.persist(product1);
        em.persist(product2);

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("findByIdWithFullAssociations")
    class FindByIdWithFullAssociations {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @DisplayName("payment, seller, store, orderItems, product가 모두 fetchJoin되어 반환된다")
            void findById_returnsOrder_withAllAssociations() {
                // given
                Order order = OrderFixture.defaultOrder()
                    .seller(em.find(Seller.class, seller.getId()))
                    .build();
                em.persist(order);

                OrderItem item1 = OrderItemFixture.defaultOrderItem()
                    .order(order)
                    .product(em.find(Product.class, product1.getId()))
                    .orderStatus(OrderStatus.PURCHASE_CONFIRMED)
                    .totalPrice(3700)
                    .build();
                order.addOrderItem(item1);
                em.persist(item1);

                Payment payment = PaymentFixture.createDefaultPayment(order);
                em.persist(payment);

                em.flush();
                em.clear();

                // when
                Optional<Order> result = sut.findByIdWithFullAssociations(order.getId());

                // then
                assertThat(result).isPresent();

                Order found = result.get();
                assertThat(found.getPayment()).isNotNull();
                assertThat(found.getSeller()).isNotNull();
                assertThat(found.getSeller().getStore()).isNotNull();
                assertThat(found.getSeller().getStore().getName()).isEqualTo(store.getName());
                assertThat(found.getOrderItems()).hasSize(1);
                assertThat(found.getOrderItems().get(0).getProduct()).isNotNull();
                assertThat(found.getOrderItems().get(0).getProduct().getTitle()).isEqualTo("비건 쌀빵 1종");
            }

            @Test
            @DisplayName("payment가 없는 주문도 leftJoin으로 결과가 반환된다")
            void findById_returnsOrder_whenPaymentIsNull() {
                // given
                Order order = OrderFixture.defaultOrder()
                    .seller(em.find(Seller.class, seller.getId()))
                    .build();
                em.persist(order);

                OrderItem item = OrderItemFixture.defaultOrderItem()
                    .order(order)
                    .product(em.find(Product.class, product1.getId()))
                    .build();
                order.addOrderItem(item);
                em.persist(item);

                em.flush();
                em.clear();

                // when
                Optional<Order> result = sut.findByIdWithFullAssociations(order.getId());

                // then
                assertThat(result).isPresent();
                assertThat(result.get().getPayment()).isNull();
                assertThat(result.get().getOrderItems()).hasSize(1);
            }

            @Test
            @DisplayName("여러 orderItem이 있어도 distinct로 중복 없이 Order 하나가 반환된다")
            void findById_returnsOneOrder_withMultipleOrderItems() {
                // given
                Order order = OrderFixture.defaultOrder()
                    .seller(em.find(Seller.class, seller.getId()))
                    .build();
                em.persist(order);

                OrderItem item1 = OrderItemFixture.defaultOrderItem()
                    .order(order).product(em.find(Product.class, product1.getId())).build();
                OrderItem item2 = OrderItemFixture.defaultOrderItem()
                    .order(order).product(em.find(Product.class, product2.getId())).build();
                order.addOrderItem(item1);
                order.addOrderItem(item2);
                em.persist(item1);
                em.persist(item2);

                em.flush();
                em.clear();

                // when
                Optional<Order> result = sut.findByIdWithFullAssociations(order.getId());

                // then
                assertThat(result).isPresent();
                assertThat(result.get().getOrderItems()).hasSize(2);
            }
        }

        @Nested
        @DisplayName("실패")
        class NotFound {

            @Test
            @DisplayName("존재하지 않는 orderId 조회 시 Optional.empty()를 반환한다")
            void findById_returnsEmpty_whenOrderNotFound() {
                // given
                Long nonExistentOrderId = 999L;

                // when
                Optional<Order> result = sut.findByIdWithFullAssociations(nonExistentOrderId);

                // then
                assertThat(result).isEmpty();
            }
        }
    }
}
