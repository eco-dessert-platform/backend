package com.bbangle.bbangle.order.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.order.domain.OrderFixture;
import com.bbangle.bbangle.fixture.order.domain.OrderItemFixture;
import com.bbangle.bbangle.fixture.seller.domain.SellerFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.order.customer.controller.dto.response.OrderReceiptResponse;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderRepository;
import com.bbangle.bbangle.seller.domain.Seller;
import com.bbangle.bbangle.store.domain.Store;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위 테스트] OrderReceiptService")
@ExtendWith(MockitoExtension.class)
class OrderReceiptServiceUnitTest {

    @InjectMocks
    private OrderReceiptService sut;

    @Mock
    private OrderRepository orderRepository;

    private Store store;
    private Seller seller;

    @BeforeEach
    void setUp() {
        store = StoreFixture.defaultStore();
        seller = SellerFixture.defaultSeller(store);
    }

    @Nested
    @DisplayName("getReceipt")
    class GetReceipt {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @DisplayName("유효한 주문 조회 시 영수증을 반환한다")
            void getReceipt_success() {
                // given
                Long memberId = 1L;
                Long orderId = 10L;

                Order order = OrderFixture.createWithMemberAndSeller(memberId, seller, 11100);
                OrderItemFixture.createWithProductAndStatus(order, store, "비건 쌀빵 1종", 3700, OrderStatus.PURCHASE_CONFIRMED);
                OrderItemFixture.createWithProductAndStatus(order, store, "비건 현미빵 1종", 3700, OrderStatus.PURCHASE_CONFIRMED);
                OrderItemFixture.createWithProductAndStatus(order, store, "비건 귀리빵 1종", 3700, OrderStatus.PURCHASE_CONFIRMED);

                given(orderRepository.findByIdWithFullAssociations(orderId)).willReturn(Optional.of(order));

                // when
                OrderReceiptResponse result = sut.getReceipt(memberId, orderId);

                // then
                assertThat(result.purchaseInfo().totalAmount()).isEqualTo(11100);
                assertThat(result.purchaseInfo().orderNumber()).isEqualTo(order.getOrderNumber());
                assertThat(result.purchaseInfo().productName()).isEqualTo("비건 쌀빵 1종 외 2건");
                assertThat(result.storeInfo().storeName()).isEqualTo(store.getName());
                assertThat(result.storeInfo().businessNumber()).isEqualTo(store.getIdentifier());
                then(orderRepository).should().findByIdWithFullAssociations(orderId);
            }

            @Test
            @DisplayName("취소 완료(CANCEL_APPROVED) 항목은 합계금액에서 제외된다")
            void getReceipt_excludes_cancelApprovedItems() {
                // given
                Long memberId = 1L;
                Long orderId = 10L;

                Order order = OrderFixture.createWithMemberAndSeller(memberId, seller, 11100);
                OrderItemFixture.createWithProductAndStatus(order, store, "비건 쌀빵 1종", 3700, OrderStatus.PURCHASE_CONFIRMED);
                OrderItemFixture.createWithProductAndStatus(order, store, "비건 현미빵 1종", 3700, OrderStatus.PURCHASE_CONFIRMED);
                OrderItemFixture.createWithProductAndStatus(order, store, "비건 귀리빵 1종", 3700, OrderStatus.CANCEL_APPROVED);

                given(orderRepository.findByIdWithFullAssociations(orderId)).willReturn(Optional.of(order));

                // when
                OrderReceiptResponse result = sut.getReceipt(memberId, orderId);

                // then
                assertThat(result.purchaseInfo().totalAmount()).isEqualTo(7400);
                assertThat(result.purchaseInfo().productName()).isEqualTo("비건 쌀빵 1종 외 1건");
            }

            @Test
            @DisplayName("반품 완료(RETURN_COMPLETED) 항목은 합계금액에서 제외된다")
            void getReceipt_excludes_returnCompletedItems() {
                // given
                Long memberId = 1L;
                Long orderId = 10L;

                Order order = OrderFixture.createWithMemberAndSeller(memberId, seller, 11100);
                OrderItemFixture.createWithProductAndStatus(order, store, "비건 쌀빵 1종", 3700, OrderStatus.PURCHASE_CONFIRMED);
                OrderItemFixture.createWithProductAndStatus(order, store, "비건 현미빵 1종", 3700, OrderStatus.RETURN_COMPLETED);

                given(orderRepository.findByIdWithFullAssociations(orderId)).willReturn(Optional.of(order));

                // when
                OrderReceiptResponse result = sut.getReceipt(memberId, orderId);

                // then
                assertThat(result.purchaseInfo().totalAmount()).isEqualTo(3700);
                assertThat(result.purchaseInfo().productName()).isEqualTo("비건 쌀빵 1종");
            }

            @Test
            @DisplayName("활성 항목이 1개이면 '외 N건' 없이 상품명만 반환된다")
            void getReceipt_singleItem_productNameWithoutSuffix() {
                // given
                Long memberId = 1L;
                Long orderId = 10L;

                Order order = OrderFixture.createWithMemberAndSeller(memberId, seller, 3700);
                OrderItemFixture.createWithProductAndStatus(order, store, "비건 쌀빵 1종", 3700, OrderStatus.PURCHASE_CONFIRMED);

                given(orderRepository.findByIdWithFullAssociations(orderId)).willReturn(Optional.of(order));

                // when
                OrderReceiptResponse result = sut.getReceipt(memberId, orderId);

                // then
                assertThat(result.purchaseInfo().productName()).isEqualTo("비건 쌀빵 1종");
            }
        }

        @Nested
        @DisplayName("실패")
        class Failure {

            @Test
            @DisplayName("존재하지 않는 주문 조회 시 ORDER_NOT_FOUND 예외가 발생한다")
            void getReceipt_throwsException_whenOrderNotFound() {
                // given
                Long memberId = 1L;
                Long orderId = 999L;

                given(orderRepository.findByIdWithFullAssociations(orderId)).willReturn(Optional.empty());

                // when & then
                BbangleException ex = catchThrowableOfType(
                    () -> sut.getReceipt(memberId, orderId),
                    BbangleException.class
                );

                assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_NOT_FOUND);
                then(orderRepository).should().findByIdWithFullAssociations(orderId);
            }

            @Test
            @DisplayName("다른 회원의 주문 조회 시 ORDER_ACCESS_DENIED 예외가 발생한다")
            void getReceipt_throwsException_whenAccessDenied() {
                // given
                Long requestMemberId = 2L;
                Long actualMemberId = 1L;
                Long orderId = 10L;

                Order order = OrderFixture.createWithMemberAndSeller(actualMemberId, seller, 3700);
                OrderItemFixture.createWithProductAndStatus(order, store, "비건 쌀빵 1종", 3700, OrderStatus.PURCHASE_CONFIRMED);

                given(orderRepository.findByIdWithFullAssociations(orderId)).willReturn(Optional.of(order));

                // when & then
                BbangleException ex = catchThrowableOfType(
                    () -> sut.getReceipt(requestMemberId, orderId),
                    BbangleException.class
                );

                assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.ORDER_ACCESS_DENIED);
                then(orderRepository).should().findByIdWithFullAssociations(orderId);
            }
        }
    }
}
