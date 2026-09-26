package com.bbangle.bbangle.order.customer.service;

import static com.bbangle.bbangle.fixture.order.customer.service.model.CreateOrderCommandFixture.amount;
import static com.bbangle.bbangle.fixture.order.customer.service.model.CreateOrderCommandFixture.create;
import static com.bbangle.bbangle.fixture.order.customer.service.model.CreateOrderCommandFixture.option;
import static com.bbangle.bbangle.fixture.order.customer.service.model.CreateOrderCommandFixture.store;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.member.domain.MemberFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.order.customer.controller.dto.response.CreateOrderResponse;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderSearchCommand;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.payment.domain.PaymentStatus;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] CustomerOrderCreateServiceIntegrationTest")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerOrderCreateServiceIntegrationTest {

    private static final int STOCK = 100;

    // 스토어A : (10,000 + 2,000) * 2 + 배송비 3,000 = 27,000
    // 스토어B : ( 5,000 + 1,000) * 1 + 배송비     0 =  6,000
    private static final int UNIT_PRICE_A = 12_000;
    private static final int UNIT_PRICE_B = 6_000;
    private static final int DELIVERY_FEE_A = 3_000;
    private static final int PRODUCT_AMOUNT = 30_000;
    private static final int TOTAL_AMOUNT = 33_000;

    @Autowired
    private CustomerOrderCreateService sut;

    @Autowired
    private CustomerOrderService customerOrderService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderItemHistoryRepository orderItemHistoryRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager em;

    private Member member;
    private Product optionA;
    private Product optionB;
    private Seller sellerA;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.defaultBuyer());

        optionA = persistOption("스토어A", "글루텐프리 케이크", 10_000, DELIVERY_FEE_A, 2_000);
        optionB = persistOption("스토어B", "저당 쿠키", 5_000, 0, 1_000);
        sellerA = sellerRepository.findAll().get(0);

        em.flush();
        em.clear();
    }

    private Product persistOption(
        String storeName, String boardTitle, int boardPrice, int deliveryFee, int optionPrice
    ) {
        Store store = storeRepository.save(StoreFixture.defaultStore(storeName));
        sellerRepository.save(SellerFixture.defaultSeller(storeName, store));

        Board board = boardRepository.save(
            BoardFixture.orderableBoard(store, boardTitle, boardPrice, deliveryFee));

        return productRepository.save(
            ProductFixture.orderableOption(board, "기본 옵션", optionPrice, STOCK));
    }

    private Long storeIdOf(Product option) {
        return option.getBoard().getStore().getId();
    }

    private Long boardIdOf(Product option) {
        return option.getBoard().getId();
    }

    private CreateOrderCommand multiStoreCommand() {
        return create(member.getId(), List.of(
            store(storeIdOf(optionA), DELIVERY_FEE_A, boardIdOf(optionA),
                option(optionA.getId(), 2, UNIT_PRICE_A)),
            store(storeIdOf(optionB), 0, boardIdOf(optionB),
                option(optionB.getId(), 1, UNIT_PRICE_B))
        ), amount(PRODUCT_AMOUNT, 0, DELIVERY_FEE_A));
    }

    private CreateOrderCommand singleStoreCommand(int quantity) {
        return create(member.getId(), List.of(
            store(storeIdOf(optionA), DELIVERY_FEE_A, boardIdOf(optionA),
                option(optionA.getId(), quantity, UNIT_PRICE_A))
        ), amount(UNIT_PRICE_A * quantity, 0, DELIVERY_FEE_A));
    }

    @DisplayName("여러 스토어를 한 번에 주문하면 스토어별 주문 N건이 결제 1건으로 묶인다")
    @Test
    void createsOneOrderPerStoreUnderSinglePayment() {
        // when
        CreateOrderResponse response = sut.create(multiStoreCommand());
        em.flush();
        em.clear();

        // then
        assertThat(response.orders()).hasSize(2);
        assertThat(response.totalAmount()).isEqualTo(TOTAL_AMOUNT);

        List<Order> orders = orderRepository.findAll();
        List<Payment> payments = paymentRepository.findAll();
        assertThat(orders).hasSize(2);
        assertThat(payments).hasSize(1);

        Payment payment = payments.get(0);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getTotalAmount()).isEqualTo(TOTAL_AMOUNT);
        assertThat(orders.stream().mapToLong(Order::getTotalAmount).sum()).isEqualTo(TOTAL_AMOUNT);
        assertThat(orders).allSatisfy(order ->
            assertThat(order.getPayment().getId()).isEqualTo(payment.getId()));
    }

    @DisplayName("응답의 스토어 순서는 요청에 담긴 순서를 따른다")
    @Test
    void keepsRequestedStoreOrder() {
        // when
        CreateOrderResponse response = sut.create(multiStoreCommand());

        // then
        assertThat(response.orders())
            .extracting(CreateOrderResponse.StoreOrderResponse::storeId)
            .containsExactly(storeIdOf(optionA), storeIdOf(optionB));
    }

    @DisplayName("스토어별 배송비가 주문에 각각 기록된다")
    @Test
    void recordsDeliveryFeePerStore() {
        // when
        CreateOrderResponse response = sut.create(multiStoreCommand());

        // then
        assertThat(response.orders())
            .extracting(CreateOrderResponse.StoreOrderResponse::deliveryFee)
            .containsExactly(3_000L, 0L);
        assertThat(response.orders())
            .extracting(CreateOrderResponse.StoreOrderResponse::totalAmount)
            .containsExactly(27_000L, 6_000L);
    }

    @DisplayName("생성 직후 주문상품은 결제대기 상태이고 재고는 차감되지 않는다")
    @Test
    void startsAsPaymentPendingWithoutStockDecrease() {
        // when
        sut.create(multiStoreCommand());
        em.flush();
        em.clear();

        // then
        List<OrderItem> orderItems = orderItemRepository.findAll();
        assertThat(orderItems).hasSize(2);
        assertThat(orderItems).allSatisfy(item -> {
            assertThat(item.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
            assertThat(item.getOrderDeliveryStatus()).isEqualTo(OrderDeliveryStatus.NONE);
        });

        assertThat(productRepository.findById(optionA.getId()).orElseThrow().getStock()).isEqualTo(STOCK);
        assertThat(productRepository.findById(optionB.getId()).orElseThrow().getStock()).isEqualTo(STOCK);
    }

    @DisplayName("주문 생성 시 주문상품 이력이 남는다")
    @Test
    void recordsOrderItemHistory() {
        // when
        sut.create(multiStoreCommand());
        em.flush();
        em.clear();

        // then
        assertThat(orderItemHistoryRepository.findAll())
            .hasSize(2)
            .allSatisfy(history ->
                assertThat(history.getOrderstatus()).isEqualTo(OrderStatus.PAYMENT_PENDING));
    }

    @DisplayName("결제 전 주문은 소비자 주문목록에 노출되지 않는다")
    @Test
    void pendingOrderIsHiddenFromCustomerOrderList() {
        // given
        sut.create(multiStoreCommand());
        em.flush();
        em.clear();

        // when
        var orders = customerOrderService.getOrders(CustomerOrderSearchCommand.builder()
            .memberId(member.getId())
            .pageable(PageRequest.of(0, 10))
            .build());

        // then
        assertThat(orders.orders().content()).isEmpty();
        assertThat(orders.orders().totalElements()).isZero();
    }

    @DisplayName("결제 전 주문은 판매자 주문목록에도 노출되지 않는다")
    @Test
    void pendingOrderIsHiddenFromSellerOrderList() {
        // given
        sut.create(multiStoreCommand());
        em.flush();
        em.clear();

        OrderSearchCommand command = new OrderSearchCommand(
            sellerA.getId(), null, null, null, PageRequest.of(0, 10));

        // when
        var orders = orderRepository.searchOrderList(command);
        var statusCounts = orderRepository.countByOrderStatus(command);

        // then
        assertThat(orders.content()).isEmpty();
        assertThat(orders.totalElements()).isZero();
        assertThat(statusCounts).doesNotContainKey(OrderStatus.PAYMENT_PENDING);
    }

    @DisplayName("결제금액이 서버 계산값과 다르면 주문이 거부된다")
    @Test
    void rejectsPaymentAmountMismatch() {
        // given : 최종금액만 1,000원으로 위변조
        CreateOrderCommand tampered = create(member.getId(), List.of(
            store(storeIdOf(optionA), DELIVERY_FEE_A, boardIdOf(optionA),
                option(optionA.getId(), 2, UNIT_PRICE_A)),
            store(storeIdOf(optionB), 0, boardIdOf(optionB),
                option(optionB.getId(), 1, UNIT_PRICE_B))
        ), new CreateOrderCommand.PaymentAmount(PRODUCT_AMOUNT, 0, DELIVERY_FEE_A, 1_000));

        // when & then
        assertThatThrownBy(() -> sut.create(tampered))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ORDER_AMOUNT_MISMATCH.getMessage());

        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(paymentRepository.findAll()).isEmpty();
    }

    @DisplayName("옵션 단가가 현재 판매가와 다르면 주문이 거부된다")
    @Test
    void rejectsOptionPriceMismatch() {
        // given : 화면에 노출됐던 단가가 10,000원이었다고 주장
        CreateOrderCommand stale = create(member.getId(), List.of(
            store(storeIdOf(optionA), DELIVERY_FEE_A, boardIdOf(optionA),
                option(optionA.getId(), 2, 10_000))
        ), amount(20_000, 0, DELIVERY_FEE_A));

        // when & then
        assertThatThrownBy(() -> sut.create(stale))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ORDER_PRICE_MISMATCH.getMessage());

        assertThat(orderRepository.findAll()).isEmpty();
    }

    @DisplayName("스토어 배송비가 서버 계산값과 다르면 주문이 거부된다")
    @Test
    void rejectsDeliveryFeeMismatch() {
        // given : 배송비를 0원으로 주장
        CreateOrderCommand stale = create(member.getId(), List.of(
            store(storeIdOf(optionA), 0, boardIdOf(optionA),
                option(optionA.getId(), 2, UNIT_PRICE_A))
        ), amount(24_000, 0, 0));

        // when & then
        assertThatThrownBy(() -> sut.create(stale))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ORDER_DELIVERY_FEE_MISMATCH.getMessage());

        assertThat(orderRepository.findAll()).isEmpty();
    }

    @DisplayName("옵션이 요청한 스토어 소속이 아니면 주문이 거부된다")
    @Test
    void rejectsWhenOptionDoesNotBelongToStore() {
        // given : 스토어A 옵션을 스토어B 소속이라고 주장
        CreateOrderCommand wrongStore = create(member.getId(), List.of(
            store(storeIdOf(optionB), DELIVERY_FEE_A, boardIdOf(optionA),
                option(optionA.getId(), 2, UNIT_PRICE_A))
        ), amount(24_000, 0, DELIVERY_FEE_A));

        // when & then
        assertThatThrownBy(() -> sut.create(wrongStore))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ORDER_INVALID_STORE.getMessage());
    }

    @DisplayName("옵션이 요청한 상품 소속이 아니면 주문이 거부된다")
    @Test
    void rejectsWhenOptionDoesNotBelongToProduct() {
        // given : 스토어A 옵션을 게시글B 소속이라고 주장
        CreateOrderCommand wrongProduct = create(member.getId(), List.of(
            store(storeIdOf(optionA), DELIVERY_FEE_A, boardIdOf(optionB),
                option(optionA.getId(), 2, UNIT_PRICE_A))
        ), amount(24_000, 0, DELIVERY_FEE_A));

        // when & then
        assertThatThrownBy(() -> sut.create(wrongProduct))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }

    @DisplayName("재고보다 많은 수량을 주문하면 거부된다")
    @Test
    void rejectsWhenStockIsNotEnough() {
        // when & then
        assertThatThrownBy(() -> sut.create(singleStoreCommand(STOCK + 1)))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_REQUEST_STOCK.getMessage());
    }

    @DisplayName("판매중이 아닌 상품은 주문할 수 없다")
    @Test
    void rejectsNotOnSaleProduct() {
        // given
        productRepository.findById(optionA.getId()).orElseThrow().getBoard().stopSale();
        em.flush();
        em.clear();

        // when & then
        assertThatThrownBy(() -> sut.create(singleStoreCommand(1)))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ORDER_PRODUCT_NOT_ON_SALE.getMessage());
    }

    @DisplayName("같은 옵션을 중복으로 주문할 수 없다")
    @Test
    void rejectsDuplicatedOption() {
        // given
        CreateOrderCommand duplicated = create(member.getId(), List.of(
            new CreateOrderCommand.StoreOrder(storeIdOf(optionA), DELIVERY_FEE_A, List.of(
                new CreateOrderCommand.ProductOrder(boardIdOf(optionA), List.of(
                    option(optionA.getId(), 1, UNIT_PRICE_A),
                    option(optionA.getId(), 1, UNIT_PRICE_A)))))
        ), amount(24_000, 0, DELIVERY_FEE_A));

        // when & then
        assertThatThrownBy(() -> sut.create(duplicated))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ORDER_DUPLICATED_OPTION.getMessage());
    }

    @DisplayName("존재하지 않는 옵션은 주문할 수 없다")
    @Test
    void rejectsUnknownOption() {
        // given
        CreateOrderCommand unknown = create(member.getId(), List.of(
            store(storeIdOf(optionA), DELIVERY_FEE_A, boardIdOf(optionA),
                option(999_999L, 1, UNIT_PRICE_A))
        ), amount(UNIT_PRICE_A, 0, DELIVERY_FEE_A));

        // when & then
        assertThatThrownBy(() -> sut.create(unknown))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }
}
