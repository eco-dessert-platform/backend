package com.bbangle.bbangle.order.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderItemFixture;
import com.bbangle.bbangle.fixture.payment.domain.PaymentFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderInfo;
import com.bbangle.bbangle.order.customer.controller.dto.response.CustomerOrderResponse.CustomerOrderPageResponse;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderSearchCommand;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.CustomerOrderCategory;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.payment.repository.PaymentRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] CustomerOrderServiceIntegrationTest")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerOrderServiceIntegrationTest {

    @Autowired
    private CustomerOrderService customerOrderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

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
    private Product product;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.builder()
            .email("buyer@bbangle.com")
            .name("홍길동")
            .nickname("길동")
            .build());

        Store store = storeRepository.save(StoreFixture.defaultStore());
        Board board = boardRepository.save(BoardFixture.defaultBoardWithStore(store, "저당 베이글 세트"));
        product = productRepository.save(ProductFixture.create(board, "저칼로리 베이글"));
    }

    private Order persistOrder(String orderNumber, LocalDateTime orderDate, OrderStatus status) {
        Order order = OrderFixture.createOrderWithNumber(orderNumber).toBuilder()
            .orderDate(orderDate)
            .totalAmount(50000)
            .member(member)
            .build();
        order = orderRepository.save(order);

        OrderItem orderItem = OrderItemFixture.defaultOrderItem()
            .orderStatus(status)
            .product(product)
            .build();
        order.addOrderItem(orderItem);
        orderItemRepository.save(orderItem);

        paymentRepository.save(PaymentFixture.createDefaultPayment(order));
        return order;
    }

    @DisplayName("회원의 주문을 주문일 최신순으로 조회한다")
    @Test
    void getOrders_returnsMemberOrdersInLatestDateOrder() {
        // given
        persistOrder("ORDER-2025-06-10-00001", LocalDateTime.of(2025, 6, 10, 10, 0), OrderStatus.PAYMENT_COMPLETED);
        persistOrder("ORDER-2025-06-12-00002", LocalDateTime.of(2025, 6, 12, 10, 0), OrderStatus.SHIPPED);
        em.flush();
        em.clear();

        CustomerOrderSearchCommand command = CustomerOrderSearchCommand.builder()
            .memberId(member.getId())
            .pageable(PageRequest.of(0, 10))
            .build();

        // when
        CustomerOrderPageResponse result = customerOrderService.getOrders(command);

        // then
        assertThat(result.orders().totalElements()).isEqualTo(2L);
        assertThat(result.orders().content()).hasSize(2);
        // 최신순(2025-06-12 먼저)
        assertThat(result.orders().content().get(0).orderNumber()).isEqualTo("ORDER-2025-06-12-00002");
        assertThat(result.orders().content().get(1).orderNumber()).isEqualTo("ORDER-2025-06-10-00001");

        CustomerOrderInfo first = result.orders().content().get(0);
        assertThat(first.orderDate()).isEqualTo(LocalDate.of(2025, 6, 12));
        assertThat(first.totalAmount()).isEqualTo(50000L);
        assertThat(first.paymentInfo()).isNotNull();
        assertThat(first.orderItems()).hasSize(1);

        var item = first.orderItems().get(0);
        assertThat(item.boardTitle()).isEqualTo("저당 베이글 세트");
        assertThat(item.productName()).isEqualTo("저칼로리 베이글");
        assertThat(item.orderStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(item.progress().category()).isEqualTo(CustomerOrderCategory.NORMAL);
        // 배송정보 없음 → 상품발송 단계(index 2)
        assertThat(item.progress().currentStepIndex()).isEqualTo(2);
    }

    @DisplayName("탭별 카운트(statusCounts)를 집계한다")
    @Test
    void getOrders_aggregatesStatusCounts() {
        // given
        persistOrder("ORDER-A", LocalDateTime.of(2025, 6, 1, 10, 0), OrderStatus.PAYMENT_COMPLETED);
        persistOrder("ORDER-B", LocalDateTime.of(2025, 6, 2, 10, 0), OrderStatus.PURCHASE_CONFIRMED);
        persistOrder("ORDER-C", LocalDateTime.of(2025, 6, 3, 10, 0), OrderStatus.RETURN_REQUESTED);
        em.flush();
        em.clear();

        CustomerOrderSearchCommand command = CustomerOrderSearchCommand.builder()
            .memberId(member.getId())
            .pageable(PageRequest.of(0, 10))
            .build();

        // when
        CustomerOrderPageResponse result = customerOrderService.getOrders(command);

        // then
        var counts = result.statusCounts();
        assertThat(counts.total()).isEqualTo(3L);
        assertThat(counts.inProgress()).isEqualTo(1L);
        assertThat(counts.purchased()).isEqualTo(1L);
        assertThat(counts.returned()).isEqualTo(1L);
        assertThat(counts.canceled()).isZero();
        assertThat(counts.exchanged()).isZero();
    }

    @DisplayName("결제 전(PAYMENT_PENDING) 주문은 목록·총건수·탭 카운트 어디에도 잡히지 않는다")
    @Test
    void getOrders_excludesPaymentPendingOrders() {
        // given: 결제완료 1건 + 결제대기 1건
        persistOrder("ORDER-PAID", LocalDateTime.of(2025, 6, 1, 10, 0), OrderStatus.PAYMENT_COMPLETED);
        persistOrder("ORDER-PENDING", LocalDateTime.of(2025, 6, 5, 10, 0), OrderStatus.PAYMENT_PENDING);
        em.flush();
        em.clear();

        CustomerOrderSearchCommand command = CustomerOrderSearchCommand.builder()
            .memberId(member.getId())
            .pageable(PageRequest.of(0, 10))
            .build();

        // when
        CustomerOrderPageResponse result = customerOrderService.getOrders(command);

        // then: 결제대기 주문이 더 최신이지만 노출되지 않아야 한다
        assertThat(result.orders().content())
            .extracting(CustomerOrderInfo::orderNumber)
            .containsExactly("ORDER-PAID");
        // 총 건수도 목록과 일치해야 한다 (어긋나면 페이지네이션이 깨진다)
        assertThat(result.orders().totalElements()).isEqualTo(1L);
        assertThat(result.statusCounts().total()).isEqualTo(1L);
    }

    @DisplayName("다른 회원의 주문은 조회되지 않는다")
    @Test
    void getOrders_excludesOtherMembersOrders() {
        // given
        persistOrder("ORDER-MINE", LocalDateTime.of(2025, 6, 1, 10, 0), OrderStatus.PAYMENT_COMPLETED);

        Member other = memberRepository.save(Member.builder()
            .email("other@bbangle.com").name("타인").nickname("타인").build());
        Order otherOrder = OrderFixture.createOrderWithNumber("ORDER-OTHER").toBuilder()
            .orderDate(LocalDateTime.of(2025, 6, 2, 10, 0))
            .member(other)
            .build();
        otherOrder = orderRepository.save(otherOrder);
        OrderItem otherItem = OrderItemFixture.defaultOrderItem().product(product).build();
        otherOrder.addOrderItem(otherItem);
        orderItemRepository.save(otherItem);
        em.flush();
        em.clear();

        CustomerOrderSearchCommand command = CustomerOrderSearchCommand.builder()
            .memberId(member.getId())
            .pageable(PageRequest.of(0, 10))
            .build();

        // when
        CustomerOrderPageResponse result = customerOrderService.getOrders(command);

        // then
        assertThat(result.orders().totalElements()).isEqualTo(1L);
        assertThat(result.orders().content().get(0).orderNumber()).isEqualTo("ORDER-MINE");
    }

    @DisplayName("존재하지 않는 회원이 조회하면 예외가 발생한다")
    @Test
    void getOrders_throwsWhenMemberNotFound() {
        // given
        CustomerOrderSearchCommand command = CustomerOrderSearchCommand.builder()
            .memberId(-1L)
            .pageable(PageRequest.of(0, 10))
            .build();

        // when & then
        assertThatThrownBy(() -> customerOrderService.getOrders(command))
            .isInstanceOf(BbangleException.class)
            .hasFieldOrPropertyWithValue("bbangleErrorCode",
                BbangleErrorCode.CUSTOMER_ORDER_MEMBER_NOT_FOUND);
    }

    @DisplayName("memberId 가 없으면 미인증 예외가 발생한다")
    @Test
    void getOrders_throwsWhenMemberIdNull() {
        // given
        CustomerOrderSearchCommand command = CustomerOrderSearchCommand.builder()
            .memberId(null)
            .pageable(PageRequest.of(0, 10))
            .build();

        // when & then
        assertThatThrownBy(() -> customerOrderService.getOrders(command))
            .isInstanceOf(BbangleException.class)
            .hasFieldOrPropertyWithValue("bbangleErrorCode",
                BbangleErrorCode.CUSTOMER_ORDER_UNAUTHORIZED);
    }
}
