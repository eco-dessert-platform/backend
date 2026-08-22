package com.bbangle.bbangle.order.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.delivery.domain.Receiver;
import com.bbangle.bbangle.delivery.domain.Sender;
import com.bbangle.bbangle.delivery.domain.Shipping;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderFixture;
import com.bbangle.bbangle.fixture.payment.domain.PaymentFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerOrderDetail;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderDetailResponse.CustomerOrderDetailItem;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderDetailCommand;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderDeliveryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.payment.repository.PaymentRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] CustomerOrderDetailServiceIntegrationTest")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerOrderDetailServiceIntegrationTest {

    @Autowired
    private CustomerOrderService customerOrderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderDeliveryRepository orderDeliveryRepository;

    @Autowired
    private MemberRepository memberRepository;

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

    private Member member;
    private Store store;
    private Product product;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
            .email("buyer@bbangle.com")
            .name("홍길동")
            .nickname("길동")
            .build());

        store = storeRepository.save(StoreFixture.defaultStore("비건빵빵이네"));
        Board board = boardRepository.save(BoardFixture.defaultBoardWithStore(store, "저당 베이글 세트"));
        product = productRepository.save(Product.builder()
            .board(board)
            .store(store)
            .title("저칼로리 베이글")
            .price(5800)
            .highProteinTag(true)
            .glutenFreeTag(true)
            .build());
    }

    private OrderItem buildItem(OrderStatus status, int productPrice, int unitPrice, int quantity) {
        return OrderItem.builder()
            .product(product)
            .quantity(quantity)
            .productPrice(productPrice)
            .unitPrice(unitPrice)
            .totalPrice(unitPrice * quantity)
            .orderStatus(status)
            .orderDeliveryStatus(OrderDeliveryStatus.PREPARING)
            .build();
    }

    private void attachDelivery(OrderItem item, OrderDeliveryStatus status, String courier, String tracking) {
        OrderDelivery delivery = OrderDelivery.create(
            Sender.of("비건빵빵이네", "027778888", "서울 마포구", "1층", "04000"),
            Receiver.of("홍길동", "01012345678", null,
                "서울특별시 강남구 테헤란로 123", "101동 1001호", "06234"),
            courier != null ? Shipping.of(courier, tracking) : Shipping.empty(),
            status,
            item);
        item.addOrderDelivery(delivery);
        orderDeliveryRepository.save(delivery);
    }

    @DisplayName("주문 상세 조회 시 결제금액은 반품·취소 상품을 제외하고 집계한다")
    @Test
    void getOrderDetail_excludesReturnedAndCanceledFromPayment() {
        // given
        Order order = OrderFixture.createOrderWithNumber("ORDER-2025-05-13-00001").toBuilder()
            .orderDate(LocalDateTime.of(2025, 5, 13, 10, 0))
            .deliveryFee(2500)
            .member(member)
            .build();
        order = orderRepository.save(order);

        OrderItem normal = buildItem(OrderStatus.SHIPPED, 5800, 4700, 2);     // 결제 대상
        OrderItem returned = buildItem(OrderStatus.RETURN_REQUESTED, 9000, 9000, 1); // 제외
        order.addOrderItem(normal);
        order.addOrderItem(returned);
        orderItemRepository.save(normal);
        orderItemRepository.save(returned);

        attachDelivery(normal, OrderDeliveryStatus.DELIVERING, "CJ대한통운", "123-456-789");

        paymentRepository.save(PaymentFixture.createDefaultPayment(order));
        Long orderId = order.getId();
        em.flush();
        em.clear();

        // when
        CustomerOrderDetail detail = customerOrderService.getOrderDetail(
            CustomerOrderDetailCommand.builder().memberId(member.getId()).orderId(orderId).build());

        // then - 헤더
        assertThat(detail.orderNumber()).isEqualTo("ORDER-2025-05-13-00001");
        assertThat(detail.orderDate()).isEqualTo("2025-05-13");

        // then - 결제 금액 (반품 상품 9000원 제외)
        assertThat(detail.payment().productAmount()).isEqualTo(5800L * 2);   // 11600
        assertThat(detail.payment().productDiscountAmount()).isEqualTo((5800L - 4700L) * 2); // 2200
        assertThat(detail.payment().deliveryFee()).isEqualTo(2500L);
        assertThat(detail.payment().finalPaymentAmount()).isEqualTo(11600L - 2200L + 2500L); // 11900
        assertThat(detail.payment().totalDiscountMessage()).isEqualTo("총 2,200원 할인 받았어요");
        assertThat(detail.payment().receiptViewable()).isTrue();
        assertThat(detail.payment().paymentInfo()).isNotNull();

        // then - 상품
        assertThat(detail.orderItems()).hasSize(2);
        CustomerOrderDetailItem normalItem = detail.orderItems().stream()
            .filter(i -> i.orderStatus() == OrderStatus.SHIPPED)
            .findFirst().orElseThrow();
        assertThat(normalItem.storeName()).isEqualTo("비건빵빵이네");
        assertThat(normalItem.boardTitle()).isEqualTo("저당 베이글 세트");
        assertThat(normalItem.productName()).isEqualTo("저칼로리 베이글");
        assertThat(normalItem.statusBadge()).isEqualTo("상품발송");
        assertThat(normalItem.discountRate()).isEqualTo(19); // round(1100/5800*100)
        assertThat(normalItem.tags()).contains("highProtein", "glutenFree");
        assertThat(normalItem.deliveryTrackable()).isTrue();
        assertThat(normalItem.courierCompany()).isEqualTo("CJ대한통운");
        assertThat(normalItem.progress().category().name()).isEqualTo("NORMAL");
    }

    @DisplayName("결제완료 상품이 있으면 배송지 변경이 가능하다")
    @Test
    void getOrderDetail_addressChangeableWhenPaymentCompletedExists() {
        // given
        Order order = OrderFixture.createOrderWithNumber("ORDER-2025-05-14-00001").toBuilder()
            .orderDate(LocalDateTime.of(2025, 5, 14, 10, 0))
            .member(member)
            .build();
        order = orderRepository.save(order);

        OrderItem paid = buildItem(OrderStatus.PAYMENT_COMPLETED, 5800, 4700, 1);
        order.addOrderItem(paid);
        orderItemRepository.save(paid);
        attachDelivery(paid, OrderDeliveryStatus.PREPARING, null, null);
        Long orderId = order.getId();
        em.flush();
        em.clear();

        // when
        CustomerOrderDetail detail = customerOrderService.getOrderDetail(
            CustomerOrderDetailCommand.builder().memberId(member.getId()).orderId(orderId).build());

        // then
        assertThat(detail.delivery().addressChangeable()).isTrue();
        assertThat(detail.delivery().recipientName()).isEqualTo("홍길동");
        assertThat(detail.delivery().address()).isEqualTo("서울특별시 강남구 테헤란로 123 101동 1001호");
        assertThat(detail.delivery().phone()).isEqualTo("01012345678");
    }

    @DisplayName("타인의 주문을 조회하면 CUSTOMER_ORDER_NOT_FOUND 예외가 발생한다")
    @Test
    void getOrderDetail_throwsWhenNotOwner() {
        // given
        Member other = memberRepository.save(Member.builder()
            .email("other@bbangle.com").name("타인").nickname("타인").build());
        Order order = OrderFixture.createOrderWithNumber("ORDER-2025-05-15-00001").toBuilder()
            .orderDate(LocalDateTime.of(2025, 5, 15, 10, 0))
            .member(other)
            .build();
        order = orderRepository.save(order);
        OrderItem item = buildItem(OrderStatus.PAYMENT_COMPLETED, 5800, 4700, 1);
        order.addOrderItem(item);
        orderItemRepository.save(item);
        Long orderId = order.getId();
        em.flush();
        em.clear();

        // when & then
        assertThatThrownBy(() -> customerOrderService.getOrderDetail(
            CustomerOrderDetailCommand.builder().memberId(member.getId()).orderId(orderId).build()))
            .isInstanceOf(BbangleException.class)
            .hasFieldOrPropertyWithValue("bbangleErrorCode", BbangleErrorCode.CUSTOMER_ORDER_NOT_FOUND);
    }

    @DisplayName("존재하지 않는 주문을 조회하면 CUSTOMER_ORDER_NOT_FOUND 예외가 발생한다")
    @Test
    void getOrderDetail_throwsWhenNotExists() {
        assertThatThrownBy(() -> customerOrderService.getOrderDetail(
            CustomerOrderDetailCommand.builder().memberId(member.getId()).orderId(999999L).build()))
            .isInstanceOf(BbangleException.class)
            .hasFieldOrPropertyWithValue("bbangleErrorCode", BbangleErrorCode.CUSTOMER_ORDER_NOT_FOUND);
    }
}
