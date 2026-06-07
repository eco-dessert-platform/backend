package com.bbangle.bbangle.order.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.DiscountType;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.SaleStatus;
import com.bbangle.bbangle.board.repository.ProductImgRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.domain.MemberDeliveryAddress;
import com.bbangle.bbangle.member.repository.MemberDeliveryAddressRepository;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.order.customer.dto.request.OrderCreateRequest;
import com.bbangle.bbangle.order.customer.dto.request.OrderPreviewRequest;
import com.bbangle.bbangle.order.customer.dto.response.OrderPreviewResponse;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.payment.domain.PaymentMethod;
import com.bbangle.bbangle.payment.repository.PaymentRepository;
import com.bbangle.bbangle.seller.repository.SellerRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.domain.model.EmailVO;
import com.bbangle.bbangle.store.domain.model.PhoneNumberVO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[단위 테스트] CustomerOrderService")
@ExtendWith(MockitoExtension.class)
class CustomerOrderServiceUnitTest {

    @Mock private MemberRepository memberRepository;
    @Mock private MemberDeliveryAddressRepository memberDeliveryAddressRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductImgRepository productImgRepository;
    @Mock private SellerRepository sellerRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PaymentRepository paymentRepository;

    @InjectMocks
    private CustomerOrderService customerOrderService;

    private static final Long MEMBER_ID = 1L;

    private Store buildStore(Long id) {
        Store store = Store.builder()
            .name("테스트 스토어")
            .identifier("12345")
            .profile("test.png")
            .introduce("소개")
            .phoneNumberVO(PhoneNumberVO.of("01012345678", "01099999999"))
            .emailVO(EmailVO.of("test@test.com"))
            .originAddressLine("서울")
            .originAddressDetail("101동")
            .build();
        ReflectionTestUtils.setField(store, "id", id);
        return store;
    }

    private Board buildBoard(Long boardId, Store store, DiscountType discountType, int price,
        Integer discountRate, Integer deliveryFee, Integer freeShippingConditions) {
        Board board = Board.builder()
            .store(store)
            .title("게시판")
            .price(price)
            .discountType(discountType)
            .discountRate(discountRate)
            .discountValue(0)
            .deliveryFee(deliveryFee)
            .saleStatus(SaleStatus.ON_SALE)
            .products(new ArrayList<>())
            .productImgs(new ArrayList<>())
            .build();
        ReflectionTestUtils.setField(board, "id", boardId);
        ReflectionTestUtils.setField(board, "freeShippingConditions", freeShippingConditions);
        return board;
    }

    private Product buildProduct(Long productId, Board board, int price) {
        Product product = Product.builder()
            .title("테스트 상품")
            .board(board)
            .price(price)
            .build();
        ReflectionTestUtils.setField(product, "id", productId);
        return product;
    }

    // -----------------------------------------------------------------------
    // Edge 1: 상품 미존재 → ORDER_PRODUCT_EMPTY
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("상품 조회 실패 시 ORDER_PRODUCT_EMPTY 예외 발생")
    class ProductNotFound {

        @Test
        @DisplayName("요청한 상품 ID가 DB에 없으면 getOrderPreview에서 예외가 발생한다")
        void getOrderPreview_productNotFound_throwsOrderProductEmpty() {
            // 요청: 상품 ID 99L, DB에는 없음
            given(productRepository.findAllWithBoardAndStoreByIdIn(anyList()))
                .willReturn(List.of()); // 빈 결과 → size mismatch

            OrderPreviewRequest request = new OrderPreviewRequest(
                List.of(new OrderPreviewRequest.OrderProductItem(99L, 1))
            );

            assertThatThrownBy(() -> customerOrderService.getOrderPreview(MEMBER_ID, request))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.ORDER_PRODUCT_EMPTY);
        }
    }

    // -----------------------------------------------------------------------
    // Edge 2: 회원 미존재 → NOTFOUND_MEMBER
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("회원 미존재 시 NOTFOUND_MEMBER 예외 발생")
    class MemberNotFound {

        @Test
        @DisplayName("존재하지 않는 memberId로 createOrders 호출 시 예외가 발생한다")
        void createOrders_memberNotFound_throwsNotFoundMember() {
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

            OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderCreateRequest.OrderProductItem(1L, 1)),
                null,
                null,
                PaymentMethod.CARD
            );

            assertThatThrownBy(() -> customerOrderService.createOrders(MEMBER_ID, request))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.NOTFOUND_MEMBER);
        }
    }

    // -----------------------------------------------------------------------
    // Edge 3: 배송지 미존재 → DELIVERY_ADDRESS_NOT_FOUND
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("배송지 미존재 시 DELIVERY_ADDRESS_NOT_FOUND 예외 발생")
    class DeliveryAddressNotFound {

        @Test
        @DisplayName("deliveryAddressId 미전달 + 기본 배송지 없으면 예외가 발생한다")
        void createOrders_noDefaultAddress_throwsDeliveryAddressNotFound() {
            Member member = Member.builder().name("홍길동").phone("010-1234-5678").build();
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
            given(memberDeliveryAddressRepository.findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(MEMBER_ID))
                .willReturn(Optional.empty());

            OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderCreateRequest.OrderProductItem(1L, 1)),
                null, // deliveryAddressId 없음
                null,
                PaymentMethod.CARD
            );

            assertThatThrownBy(() -> customerOrderService.createOrders(MEMBER_ID, request))
                .isInstanceOf(BbangleException.class)
                .extracting(e -> ((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.DELIVERY_ADDRESS_NOT_FOUND);
        }
    }

    // -----------------------------------------------------------------------
    // Success: RATE 할인 + 배송비 임계 미달 계산 검증
    // -----------------------------------------------------------------------
    @Nested
    @DisplayName("getOrderPreview 성공 — 할인율·배송비 계산 검증")
    class OrderPreviewSuccess {

        @Test
        @DisplayName("RATE 10% 할인 상품 1개(수량2), 배송비 미달 시 배송비 포함 합계 반환")
        void getOrderPreview_rateDiscount_calculatesCorrectTotals() {
            // 상품가 10,000원, RATE 10% 할인 → 단가 9,000원
            // 수량 2 → 소계 18,000원, 무료배송 조건 30,000원 → 배송비 3,000원 부과
            Store store = buildStore(10L);
            Board board = buildBoard(20L, store, DiscountType.RATE, 10_000, 10, 3_000, 30_000);
            Product product = buildProduct(1L, board, 10_000);

            given(productRepository.findAllWithBoardAndStoreByIdIn(anyList()))
                .willReturn(List.of(product));
            given(productImgRepository.findThumbnailsByBoardIdIn(anyList()))
                .willReturn(List.of());
            given(memberDeliveryAddressRepository.findByMemberIdAndIsDefaultTrueAndIsDeletedFalse(MEMBER_ID))
                .willReturn(Optional.empty());

            OrderPreviewRequest request = new OrderPreviewRequest(
                List.of(new OrderPreviewRequest.OrderProductItem(1L, 2))
            );

            OrderPreviewResponse response = customerOrderService.getOrderPreview(MEMBER_ID, request);

            assertThat(response.totalProductAmount()).isEqualTo(18_000); // 9,000 × 2
            assertThat(response.totalDeliveryFee()).isEqualTo(3_000);    // 배송비 미달
            assertThat(response.totalPaymentAmount()).isEqualTo(21_000); // 18,000 + 3,000
        }
    }
}
