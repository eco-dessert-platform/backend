package com.bbangle.bbangle.order.seller.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderItemFixture;
import com.bbangle.bbangle.fixture.payment.domain.PaymentFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.CompletedOrderStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.payment.repository.PaymentRepository;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("[통합테스트] SellerOrderController - 완료 주문 API")
class SellerOrderCompletedApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager em;

    private Seller sellerA;
    private Seller sellerB;

    @BeforeEach
    void setUp() {
        // Seller A & Store A
        Store storeA = storeRepository.save(StoreFixture.defaultStore("Store A"));
        sellerA = sellerRepository.save(SellerFixture.createDefaultSeller(storeA));

        // Seller B & Store B
        Store storeB = storeRepository.save(StoreFixture.defaultStore("Store B"));
        sellerB = sellerRepository.save(SellerFixture.createDefaultSeller(storeB));

        // Board & Product for Seller A
        Board boardA = boardRepository.save(BoardFixture.defaultBoardWithStore(storeA, "Product A"));
        Product productA = productRepository.save(ProductFixture.create(boardA, "Product A Item"));

        // Order for Seller A (PURCHASE_CONFIRMED -> PURCHASED status)
        Order orderA = OrderFixture.createOrderWithNumber("ORD-A-001").toBuilder()
            .seller(sellerA).buyerName("Buyer A").build();
        orderRepository.save(orderA);
        OrderItem itemA = OrderItemFixture.defaultOrderItem()
            .product(productA)
            .order(orderA)
            .orderStatus(OrderStatus.PURCHASE_CONFIRMED)
            .build();
        orderA.addOrderItem(itemA);
        orderItemRepository.save(itemA);
        paymentRepository.save(PaymentFixture.createDefaultPayment(orderA));

        // Order for Seller B
        Board boardB = boardRepository.save(BoardFixture.defaultBoardWithStore(storeB, "Product B"));
        Product productB = productRepository.save(ProductFixture.create(boardB, "Product B Item"));
        Order orderB = OrderFixture.createOrderWithNumber("ORD-B-001").toBuilder()
            .seller(sellerB).buyerName("Buyer B").build();
        orderRepository.save(orderB);
        OrderItem itemB = OrderItemFixture.defaultOrderItem()
            .product(productB)
            .order(orderB)
            .orderStatus(OrderStatus.PURCHASE_CONFIRMED)
            .build();
        orderB.addOrderItem(itemB);
        orderItemRepository.save(itemB);
        paymentRepository.save(PaymentFixture.createDefaultPayment(orderB));

        em.flush();
        em.clear();
    }

    @DisplayName("완료 주문 조회 - 타 판매자의 주문은 조회되지 않아야 함 (판매자 격리 엣지 케이스)")
    @Test
    void getCompletedOrders_IsolationBySeller() throws Exception {
        // When: Seller A가 자신의 완료 주문 조회
        mvc.perform(get("/api/v1/seller/orders/completed")
                .with(authentication(new UsernamePasswordAuthenticationToken(
                    sellerA.getId(), "N/A", List.of(new SimpleGrantedAuthority("ROLE_SELLER")))))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.orders.content.length()").value(1))
            .andExpect(jsonPath("$.result.orders.content[0].orderNum").value("ORD-A-001"));
    }

    @DisplayName("완료 주문 조회 - 특정 상태(CANCELED) 필터링 시 결과 없음 (엣지 케이스)")
    @Test
    void getCompletedOrders_FilterByStatus_NoMatch() throws Exception {
        // When: Seller A가 '취소' 상태인 주문 조회 (현재는 구매확정만 있음)
        mvc.perform(get("/api/v1/seller/orders/completed")
                .with(authentication(new UsernamePasswordAuthenticationToken(
                    sellerA.getId(), "N/A", List.of(new SimpleGrantedAuthority("ROLE_SELLER")))))
                .param("status", CompletedOrderStatus.CANCELED.name())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.orders.content").isEmpty())
            .andExpect(jsonPath("$.result.statusCounts.purchased").value(1))
            .andExpect(jsonPath("$.result.statusCounts.canceled").value(0));
    }

    @DisplayName("완료 주문 조회 - 페이징 적용 확인 (엣지 케이스: 첫 페이지 조회)")
    @Test
    void getCompletedOrders_PaginationAndSorting() throws Exception {
        // When: Seller A가 페이징 정보를 담아 호출
        mvc.perform(get("/api/v1/seller/orders/completed")
                .with(authentication(new UsernamePasswordAuthenticationToken(
                    sellerA.getId(), "N/A", List.of(new SimpleGrantedAuthority("ROLE_SELLER")))))
                .param("page", "0")
                .param("size", "1")
                .param("sort", "id,desc")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.result.orders.page").value(0))
            .andExpect(jsonPath("$.result.orders.size").value(1))
            .andExpect(jsonPath("$.result.orders.totalElements").value(1));
    }

}
