package com.bbangle.bbangle.order.seller.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.common.dto.SearchFormDto.DefaultSearchCondition;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderItemFixture;
import com.bbangle.bbangle.fixture.payment.domain.PaymentFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.CourierCompany;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.seller.controller.dto.response.OrderResponse;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.payment.repository.PaymentRepository;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] SellerOrderServiceIntegrationTest")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SellerOrderServiceIntegrationTest {

    @Autowired
    private SellerOrderService sellerOrderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager em;
    
    private Store store;
    private Seller seller;
    private Order order;
    private Payment payment;

    @BeforeEach
    void setUp() {
        // Store 저장
        store = StoreFixture.defaultStore();
        store = storeRepository.save(store);

        // Seller 저장
        seller = SellerFixture.createDefaultSeller(store);
        seller = sellerRepository.save(seller);

        // Board와 Product 저장
        Store boardStore = StoreFixture.defaultStore();
        boardStore = storeRepository.save(boardStore);
        Board board = BoardFixture.defaultBoardWithStore(boardStore, "카카오커피");
        board = boardRepository.save(board);
        Product product = ProductFixture.create(board, "카카오커피");
        product = productRepository.save(product);

        // Order 저장
        order = OrderFixture.createOrderWithNumber("ORDER-2025-01-01-00001")
            .toBuilder()
            .totalAmount(50000)
            .buyerName("홍길동")
            .seller(seller)
            .build();
        order = orderRepository.save(order);

        // OrderItem 저장
        OrderItem orderItem = OrderItemFixture.createOrderItemWithProduct(product);
        order.addOrderItem(orderItem);
        orderItemRepository.save(orderItem);

        // Payment 저장
        payment = PaymentFixture.createDefaultPayment(order);
        payment = paymentRepository.save(payment);

        // 영속성 컨텍스트 초기화 (fetch join 쿼리가 DB에서 완전한 데이터를 가져오도록)
        em.flush();
        em.clear();
    }

    @DisplayName("주문 검색 시 Page 객체로 올바르게 반환되는지 확인")
    @Test
    void givenOrderSearchRequest_whenOrderSearch_thenReturnsPageWithCorrectData() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);
        DefaultSearchCondition searchCondition = new DefaultSearchCondition();
        searchCondition.setStartDate(LocalDate.of(2025, 1, 1));
        searchCondition.setEndDate(LocalDate.of(2025, 1, 31));

        OrderSearchCommand command = OrderSearchCommand.builder()
            .sellerId(seller.getId())
            .orderDeliveryStatus(null)
            .orderStatus(null)
            .searchCondition(searchCondition)
            .page(pageable)
            .build();

        // when
        BbanglePageResponse<OrderResponse.OrderSearchResponse> result = sellerOrderService.orderSearch(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content()).asList().hasSize(1);

        OrderResponse.OrderSearchResponse response = result.content().get(0);

        // 기본 정보 검증
        assertThat(response.orderNumber()).isEqualTo("ORDER-2025-01-01-00001");
        assertThat(response.totalOrderPrice()).isEqualTo("50000");
        assertThat(response.recipientName()).isEqualTo("홍길동");

        // 주문 상품 정보 검증
        assertThat(response.orderItems()).isNotNull().asList().hasSize(1);

        var orderItemList = response.orderItems().get(0);
        assertThat(orderItemList.orderNumber()).isEqualTo("ORDER-2025-01-01-00001");
        assertThat(orderItemList.orderStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
        assertThat(orderItemList.orderItemInfo().itemName()).isEqualTo("카카오커피");
        assertThat(orderItemList.orderItemInfo().quantity()).isEqualTo(2);
        assertThat(orderItemList.orderItemInfo().unitPrice()).isEqualTo(25000L);
        assertThat(orderItemList.orderItemInfo().totalPrice()).isEqualTo(50000L);

        // 배송 정보 검증 (OrderItem에 OrderDelivery 없으므로 기본값 - 품목 수준)
        assertThat(orderItemList.orderDeliveryStatus()).isEqualTo(OrderDeliveryStatus.PREPARING);
        assertThat(orderItemList.courierCompany()).isEqualTo(CourierCompany.NONE);
        assertThat(orderItemList.trackingNumber()).isEqualTo("-");

        // 판매자 정보 검증
        assertThat(response.sellerId()).isEqualTo(seller.getId());

        // 결제 정보 검증
        assertThat(response.paymentInfo()).isNotNull();
    }
}
