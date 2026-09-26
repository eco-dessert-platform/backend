package com.bbangle.bbangle.order.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.member.domain.MemberFixture;
import static com.bbangle.bbangle.fixture.order.customer.service.model.CreateOrderCommandFixture.amount;
import static com.bbangle.bbangle.fixture.order.customer.service.model.CreateOrderCommandFixture.create;
import static com.bbangle.bbangle.fixture.order.customer.service.model.CreateOrderCommandFixture.createSingle;
import static com.bbangle.bbangle.fixture.order.customer.service.model.CreateOrderCommandFixture.option;
import static com.bbangle.bbangle.fixture.order.customer.service.model.CreateOrderCommandFixture.store;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.order.customer.controller.dto.response.CreateOrderResponse;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.PaymentAmount;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.ProductOrder;
import com.bbangle.bbangle.order.customer.service.model.CreateOrderCommand.StoreOrder;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.OrderItemHistory;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderDeliveryRepository;
import com.bbangle.bbangle.order.repository.OrderItemHistoryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.payment.domain.Payment;
import com.bbangle.bbangle.payment.domain.PaymentStatus;
import com.bbangle.bbangle.payment.repository.PaymentRepository;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 주문 생성 서비스 단위 테스트.
 *
 * <p>DB 없이 검증 순서와 저장 호출을 확인한다.
 * 금액 산식 자체는 {@link OrderAmountCalculatorTest} 가, 실제 영속화는 통합 테스트가 담당한다.
 */
@DisplayName("[단위 테스트] CustomerOrderCreateService")
@ExtendWith(MockitoExtension.class)
class CustomerOrderCreateServiceUnitTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long STORE_A_ID = 1L;
    private static final Long STORE_B_ID = 2L;
    private static final Long BOARD_A_ID = 10L;
    private static final Long BOARD_B_ID = 20L;
    private static final Long OPTION_A_ID = 101L;
    private static final Long OPTION_B_ID = 201L;
    private static final int STOCK = 100;

    // 스토어A : (10,000 + 2,000) * 2 + 배송비 3,000 = 27,000
    // 스토어B : ( 5,000 + 1,000) * 1 + 배송비     0 =  6,000
    private static final int UNIT_PRICE_A = 12_000;
    private static final int UNIT_PRICE_B = 6_000;
    private static final int DELIVERY_FEE_A = 3_000;
    private static final int TOTAL_AMOUNT = 33_000;

    @InjectMocks
    private CustomerOrderCreateService sut;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderDeliveryRepository orderDeliveryRepository;

    @Mock
    private OrderItemHistoryRepository orderItemHistoryRepository;

    private Member member;
    private Product optionA;
    private Product optionB;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createWithId(MEMBER_ID);

        optionA = persistedOption(STORE_A_ID, BOARD_A_ID, "글루텐프리 케이크", 10_000, DELIVERY_FEE_A, OPTION_A_ID, 2_000);
        optionB = persistedOption(STORE_B_ID, BOARD_B_ID, "저당 쿠키", 5_000, 0, OPTION_B_ID, 1_000);

    }

    private void givenMemberFound() {
        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
    }

    /** 저장 호출이 인자를 그대로 돌려주도록 한다. 성공 경로에서만 필요하다. */
    private void givenSaveReturnsArgument() {
        given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));
        given(orderRepository.save(any(Order.class))).willAnswer(inv -> inv.getArgument(0));
    }

    private static Product persistedOption(
        Long storeId, Long boardId, String boardTitle, int boardPrice, int deliveryFee,
        Long optionId, int addedPrice
    ) {
        Store store = StoreFixture.withId(StoreFixture.defaultStore("스토어" + storeId), storeId);
        Board board = BoardFixture.withId(
            BoardFixture.orderableBoard(store, boardTitle, boardPrice, deliveryFee), boardId);
        return ProductFixture.withId(
            ProductFixture.orderableOption(board, "기본 옵션", addedPrice, STOCK), optionId);
    }

    private void givenProductsFound(Product... products) {
        given(productRepository.findAllWithBoardAndStoreByIdIn(anyList()))
            .willReturn(List.of(products));
    }

    private void givenSellersFound(Product... products) {
        List<Seller> sellers = List.of(products).stream()
            .map(product -> {
                Store store = product.getBoard().getStore();
                return SellerFixture.defaultSeller("판매자" + store.getId(), store);
            })
            .toList();
        given(sellerRepository.findByStoreIdIn(anyList())).willReturn(sellers);
    }

    private CreateOrderCommand multiStoreCommand() {
        return create(MEMBER_ID, List.of(
            store(STORE_A_ID, DELIVERY_FEE_A, BOARD_A_ID, option(OPTION_A_ID, 2, UNIT_PRICE_A)),
            store(STORE_B_ID, 0, BOARD_B_ID, option(OPTION_B_ID, 1, UNIT_PRICE_B))
        ), amount(30_000, 0, DELIVERY_FEE_A));
    }

    private CreateOrderCommand singleStoreCommand(int quantity) {
        return createSingle(MEMBER_ID, STORE_A_ID, BOARD_A_ID, OPTION_A_ID,
            quantity, UNIT_PRICE_A, DELIVERY_FEE_A);
    }

    @DisplayName("스토어별로 주문을 나누고 결제 1건으로 묶는다")
    @Test
    void createsOneOrderPerStoreUnderSinglePayment() {
        // given
        givenMemberFound();
        givenSaveReturnsArgument();
        givenProductsFound(optionA, optionB);
        givenSellersFound(optionA, optionB);

        // when
        CreateOrderResponse response = sut.create(multiStoreCommand());

        // then : 결제는 1번, 주문은 스토어 수만큼 저장된다
        then(paymentRepository).should(times(1)).save(any(Payment.class));
        then(orderRepository).should(times(2)).save(any(Order.class));
        then(orderItemRepository).should(times(2)).save(any(OrderItem.class));

        assertThat(response.totalAmount()).isEqualTo(TOTAL_AMOUNT);
        assertThat(response.orders()).hasSize(2);
    }

    @DisplayName("결제는 PENDING 상태로 주문 총액을 들고 생성된다")
    @Test
    void createsPendingPaymentWithTotalAmount() {
        // given
        givenMemberFound();
        givenSaveReturnsArgument();
        givenProductsFound(optionA, optionB);
        givenSellersFound(optionA, optionB);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);

        // when
        sut.create(multiStoreCommand());

        // then
        then(paymentRepository).should().save(captor.capture());
        Payment payment = captor.getValue();
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(payment.getTotalAmount()).isEqualTo(TOTAL_AMOUNT);
        assertThat(payment.getMember()).isEqualTo(member);
        assertThat(payment.getPaymentNumber()).startsWith("PAY-");
    }

    @DisplayName("주문상품은 결제대기 상태로 생성되고 이력이 함께 남는다")
    @Test
    void createsPendingOrderItemWithHistory() {
        // given
        givenMemberFound();
        givenSaveReturnsArgument();
        givenProductsFound(optionA);
        givenSellersFound(optionA);

        ArgumentCaptor<OrderItem> itemCaptor = ArgumentCaptor.forClass(OrderItem.class);

        // when
        sut.create(singleStoreCommand(2));

        // then
        then(orderItemRepository).should().save(itemCaptor.capture());
        OrderItem orderItem = itemCaptor.getValue();
        assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(orderItem.getProductPrice()).isEqualTo(UNIT_PRICE_A);
        assertThat(orderItem.getUnitPrice()).isEqualTo(UNIT_PRICE_A);
        assertThat(orderItem.getTotalPrice()).isEqualTo(24_000);

        then(orderItemHistoryRepository).should(times(1)).save(any(OrderItemHistory.class));
        then(orderDeliveryRepository).should(times(1)).save(any(OrderDelivery.class));
    }

    @DisplayName("배송지 요청사항이 배송 정보에 기록된다")
    @Test
    void recordsShippingAddressAndMemo() {
        // given
        givenMemberFound();
        givenSaveReturnsArgument();
        givenProductsFound(optionA);
        givenSellersFound(optionA);

        ArgumentCaptor<OrderDelivery> captor = ArgumentCaptor.forClass(OrderDelivery.class);

        // when
        sut.create(singleStoreCommand(2));

        // then
        then(orderDeliveryRepository).should().save(captor.capture());
        OrderDelivery delivery = captor.getValue();
        assertThat(delivery.getReceiver().getRecipientName()).isEqualTo("홍길동");
        assertThat(delivery.getReceiver().getRecipientZipCode()).isEqualTo("13529");
        assertThat(delivery.getShipping().getDeliveryMemo()).isEqualTo("문 앞에 놔주세요");
    }

    @DisplayName("주문명은 '첫 상품명 외 N건' 으로 조립된다")
    @Test
    void buildsOrderName() {
        // given
        givenMemberFound();
        givenSaveReturnsArgument();
        givenProductsFound(optionA, optionB);
        givenSellersFound(optionA, optionB);

        // when
        CreateOrderResponse response = sut.create(multiStoreCommand());

        // then
        assertThat(response.orderName()).isEqualTo("글루텐프리 케이크 외 1건");
    }

    @DisplayName("단일 상품 주문이면 주문명에 '외 N건' 이 붙지 않는다")
    @Test
    void buildsOrderNameWithoutSuffixForSingleItem() {
        // given
        givenMemberFound();
        givenSaveReturnsArgument();
        givenProductsFound(optionA);
        givenSellersFound(optionA);

        // when
        CreateOrderResponse response =
            sut.create(singleStoreCommand(2));

        // then
        assertThat(response.orderName()).isEqualTo("글루텐프리 케이크");
    }

    @DisplayName("클라이언트가 보낸 금액이 서버 계산값과 다르면 아무것도 저장하지 않는다")
    @Test
    void rejectsAmountMismatchBeforeAnyWrite() {
        // given
        givenMemberFound();
        givenProductsFound(optionA, optionB);
        givenSellersFound(optionA, optionB);

        CreateOrderCommand tampered = create(MEMBER_ID, List.of(
            store(STORE_A_ID, DELIVERY_FEE_A, BOARD_A_ID, option(OPTION_A_ID, 2, UNIT_PRICE_A)),
            store(STORE_B_ID, 0, BOARD_B_ID, option(OPTION_B_ID, 1, UNIT_PRICE_B))
        ), new PaymentAmount(30_000, 0, DELIVERY_FEE_A, 1_000));

        // when & then
        assertThatThrownBy(() -> sut.create(tampered))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ORDER_AMOUNT_MISMATCH.getMessage());

        then(paymentRepository).should(never()).save(any(Payment.class));
        then(orderRepository).should(never()).save(any(Order.class));
    }

    @DisplayName("옵션 단가가 현재 판매가와 다르면 거부한다")
    @Test
    void rejectsOptionPriceMismatch() {
        // given : 화면 단가가 10,000원이었다고 주장
        givenMemberFound();
        givenProductsFound(optionA);

        CreateOrderCommand stale = create(MEMBER_ID, List.of(
            store(STORE_A_ID, DELIVERY_FEE_A, BOARD_A_ID, option(OPTION_A_ID, 2, 10_000))
        ), amount(20_000, 0, DELIVERY_FEE_A));

        // when & then
        assertThatThrownBy(() -> sut.create(stale))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ORDER_PRICE_MISMATCH.getMessage());

        then(orderRepository).should(never()).save(any(Order.class));
    }

    @DisplayName("스토어 배송비가 서버 계산값과 다르면 거부한다")
    @Test
    void rejectsDeliveryFeeMismatch() {
        // given : 배송비를 0원으로 주장
        givenMemberFound();
        givenProductsFound(optionA);
        givenSellersFound(optionA);

        CreateOrderCommand stale = create(MEMBER_ID, List.of(
            store(STORE_A_ID, 0, BOARD_A_ID, option(OPTION_A_ID, 2, UNIT_PRICE_A))
        ), amount(24_000, 0, 0));

        // when & then
        assertThatThrownBy(() -> sut.create(stale))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ORDER_DELIVERY_FEE_MISMATCH.getMessage());

        then(orderRepository).should(never()).save(any(Order.class));
    }

    @DisplayName("옵션이 요청한 스토어 소속이 아니면 거부한다")
    @Test
    void rejectsWhenOptionDoesNotBelongToStore() {
        // given : 스토어A 옵션을 스토어B 소속이라고 주장
        givenMemberFound();
        givenProductsFound(optionA);

        CreateOrderCommand wrongStore = create(MEMBER_ID, List.of(
            store(STORE_B_ID, DELIVERY_FEE_A, BOARD_A_ID, option(OPTION_A_ID, 2, UNIT_PRICE_A))
        ), amount(24_000, 0, DELIVERY_FEE_A));

        // when & then
        assertThatThrownBy(() -> sut.create(wrongStore))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ORDER_INVALID_STORE.getMessage());

        then(orderRepository).should(never()).save(any(Order.class));
    }

    @DisplayName("옵션이 요청한 상품 소속이 아니면 거부한다")
    @Test
    void rejectsWhenOptionDoesNotBelongToProduct() {
        // given : 스토어A 옵션을 게시글B 소속이라고 주장
        givenMemberFound();
        givenProductsFound(optionA);

        CreateOrderCommand wrongProduct = create(MEMBER_ID, List.of(
            store(STORE_A_ID, DELIVERY_FEE_A, BOARD_B_ID, option(OPTION_A_ID, 2, UNIT_PRICE_A))
        ), amount(24_000, 0, DELIVERY_FEE_A));

        // when & then
        assertThatThrownBy(() -> sut.create(wrongProduct))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.PRODUCT_NOT_FOUND.getMessage());

        then(orderRepository).should(never()).save(any(Order.class));
    }

    @DisplayName("존재하지 않는 회원이면 상품을 조회하기 전에 실패한다")
    @Test
    void rejectsUnknownMemberBeforeLoadingProducts() {
        // given
        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
            sut.create(singleStoreCommand(1)))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.CUSTOMER_ORDER_MEMBER_NOT_FOUND.getMessage());

        then(productRepository).should(never()).findAllWithBoardAndStoreByIdIn(anyList());
    }

    @DisplayName("같은 옵션이 중복되면 상품을 조회하기 전에 실패한다")
    @Test
    void rejectsDuplicatedOptionBeforeLoadingProducts() {
        // given
        givenMemberFound();
        CreateOrderCommand duplicated = create(MEMBER_ID, List.of(
            new StoreOrder(STORE_A_ID, DELIVERY_FEE_A, List.of(
                new ProductOrder(BOARD_A_ID, List.of(
                    option(OPTION_A_ID, 1, UNIT_PRICE_A),
                    option(OPTION_A_ID, 1, UNIT_PRICE_A)))))
        ), amount(24_000, 0, DELIVERY_FEE_A));

        // when & then
        assertThatThrownBy(() -> sut.create(duplicated))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ORDER_DUPLICATED_OPTION.getMessage());

        then(productRepository).should(never()).findAllWithBoardAndStoreByIdIn(anyList());
    }

    @DisplayName("요청한 옵션 중 하나라도 없으면 실패한다")
    @Test
    void rejectsWhenAnyOptionIsMissing() {
        // given : 2건을 요청했는데 1건만 조회된다
        givenMemberFound();
        givenProductsFound(optionA);

        // when & then
        assertThatThrownBy(() -> sut.create(multiStoreCommand()))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.PRODUCT_NOT_FOUND.getMessage());

        then(orderRepository).should(never()).save(any(Order.class));
    }

    @DisplayName("판매중이 아닌 상품은 주문할 수 없다")
    @Test
    void rejectsNotOnSaleProduct() {
        // given
        givenMemberFound();
        optionA.getBoard().stopSale();
        givenProductsFound(optionA);

        // when & then
        assertThatThrownBy(() ->
            sut.create(singleStoreCommand(1)))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.ORDER_PRODUCT_NOT_ON_SALE.getMessage());

        then(orderRepository).should(never()).save(any(Order.class));
    }

    @DisplayName("재고보다 많은 수량은 주문할 수 없다")
    @Test
    void rejectsWhenStockIsNotEnough() {
        // given
        givenMemberFound();
        givenProductsFound(optionA);

        // when & then
        assertThatThrownBy(() ->
            sut.create(singleStoreCommand(STOCK + 1)))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.INVALID_REQUEST_STOCK.getMessage());

        then(orderRepository).should(never()).save(any(Order.class));
    }

    @DisplayName("스토어에 연결된 판매자가 없으면 주문할 수 없다")
    @Test
    void rejectsWhenSellerIsMissing() {
        // given : 스토어는 2개인데 판매자는 1명만 조회된다
        givenMemberFound();
        givenProductsFound(optionA, optionB);
        givenSellersFound(optionA);

        // when & then
        assertThatThrownBy(() -> sut.create(multiStoreCommand()))
            .isInstanceOf(BbangleException.class)
            .hasMessageContaining(BbangleErrorCode.SELLER_NOT_FOUND.getMessage());

        then(orderRepository).should(never()).save(any(Order.class));
    }
}
