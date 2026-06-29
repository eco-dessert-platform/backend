package com.bbangle.bbangle.order.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.bbangle.bbangle.delivery.domain.Shipping;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.order.domain.OrderItemFixture;
import com.bbangle.bbangle.order.domain.OrderDelivery;
import com.bbangle.bbangle.order.domain.OrderItem;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.repository.OrderDeliveryRepository;
import com.bbangle.bbangle.order.repository.OrderItemRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[단위 테스트] PurchaseConfirmService")
@ExtendWith(MockitoExtension.class)
class PurchaseConfirmServiceUnitTest {

    @InjectMocks
    private PurchaseConfirmService sut;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private OrderDeliveryRepository orderDeliveryRepository;

    private OrderItem shippedOrderItem(Long id) {
        OrderItem orderItem = OrderItemFixture.createOrderItemWithStatus(OrderStatus.SHIPPED);
        ReflectionTestUtils.setField(orderItem, "id", id);
        return orderItem;
    }

    private OrderDelivery deliveryWithStatus(OrderItem orderItem, OrderDeliveryStatus status) {
        return OrderDelivery.create(null, null, Shipping.of("CJ대한통운", "1234567890"), status, orderItem);
    }

    @Nested
    @DisplayName("수동 구매확정 (confirm)")
    class ConfirmTest {

        private static final Long MEMBER_ID = 1L;
        private static final Long ORDER_ID = 100L;
        private static final Long ORDER_ITEM_ID = 10L;

        @DisplayName("본인 소유의 배송완료된 주문상품이면 PURCHASE_CONFIRMED로 전환되고 확정 시각이 기록된다")
        @Test
        void givenOwnedDeliveredItem_whenConfirm_thenPurchaseConfirmed() {
            // given
            OrderItem orderItem = shippedOrderItem(ORDER_ITEM_ID);

            given(orderItemRepository.findOwnedOrderItem(MEMBER_ID, ORDER_ID, ORDER_ITEM_ID))
                .willReturn(Optional.of(orderItem));
            given(orderDeliveryRepository.findByOrderItemId(ORDER_ITEM_ID))
                .willReturn(Optional.of(deliveryWithStatus(orderItem, OrderDeliveryStatus.DELIVERED)));

            // when
            Long result = sut.confirm(MEMBER_ID, ORDER_ID, ORDER_ITEM_ID);

            // then
            assertThat(result).isEqualTo(ORDER_ITEM_ID);
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_CONFIRMED);
            assertThat(orderItem.getPurchaseConfirmedAt()).isNotNull();
        }

        @DisplayName("본인 소유가 아니거나 존재하지 않는 주문상품이면 CUSTOMER_ORDER_ITEM_NOT_FOUND 예외가 발생한다")
        @Test
        void givenNotOwnedItem_whenConfirm_thenThrowsNotFound() {
            // given
            given(orderItemRepository.findOwnedOrderItem(MEMBER_ID, ORDER_ID, ORDER_ITEM_ID))
                .willReturn(Optional.empty());

            // when
            BbangleException result = assertThrows(BbangleException.class,
                () -> sut.confirm(MEMBER_ID, ORDER_ID, ORDER_ITEM_ID));

            // then
            assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.CUSTOMER_ORDER_ITEM_NOT_FOUND);
            then(orderDeliveryRepository).should(never()).findByOrderItemId(ORDER_ITEM_ID);
        }

        @DisplayName("배송완료(DELIVERED) 상태가 아니면 PURCHASE_CONFIRM_NOT_ALLOWED 예외가 발생하고 상태가 변경되지 않는다")
        @Test
        void givenNotDeliveredItem_whenConfirm_thenThrowsNotAllowed() {
            // given
            OrderItem orderItem = shippedOrderItem(ORDER_ITEM_ID);

            given(orderItemRepository.findOwnedOrderItem(MEMBER_ID, ORDER_ID, ORDER_ITEM_ID))
                .willReturn(Optional.of(orderItem));
            given(orderDeliveryRepository.findByOrderItemId(ORDER_ITEM_ID))
                .willReturn(Optional.of(deliveryWithStatus(orderItem, OrderDeliveryStatus.DELIVERING)));

            // when
            BbangleException result = assertThrows(BbangleException.class,
                () -> sut.confirm(MEMBER_ID, ORDER_ID, ORDER_ITEM_ID));

            // then
            assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.PURCHASE_CONFIRM_NOT_ALLOWED);
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(orderItem.getPurchaseConfirmedAt()).isNull();
        }

        @DisplayName("배송 정보가 아예 없으면 PURCHASE_CONFIRM_NOT_ALLOWED 예외가 발생한다")
        @Test
        void givenNoDelivery_whenConfirm_thenThrowsNotAllowed() {
            // given
            OrderItem orderItem = shippedOrderItem(ORDER_ITEM_ID);

            given(orderItemRepository.findOwnedOrderItem(MEMBER_ID, ORDER_ID, ORDER_ITEM_ID))
                .willReturn(Optional.of(orderItem));
            given(orderDeliveryRepository.findByOrderItemId(ORDER_ITEM_ID))
                .willReturn(Optional.empty());

            // when
            BbangleException result = assertThrows(BbangleException.class,
                () -> sut.confirm(MEMBER_ID, ORDER_ID, ORDER_ITEM_ID));

            // then
            assertThat(result.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.PURCHASE_CONFIRM_NOT_ALLOWED);
            assertThat(orderItem.getOrderStatus()).isEqualTo(OrderStatus.SHIPPED);
        }
    }

    @Nested
    @DisplayName("자동 구매확정 배치 (autoConfirmExpired)")
    class AutoConfirmTest {

        @DisplayName("배송완료 후 7일이 지난 대상 주문상품을 모두 구매확정하고 처리 건수를 반환한다")
        @Test
        void givenExpiredTargets_whenAutoConfirm_thenAllConfirmedAndCountReturned() {
            // given
            OrderItem target1 = shippedOrderItem(10L);
            OrderItem target2 = shippedOrderItem(11L);

            given(orderItemRepository.findAutoConfirmTargets(org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of(target1, target2));

            // when
            int confirmedCount = sut.autoConfirmExpired();

            // then
            assertThat(confirmedCount).isEqualTo(2);
            assertThat(target1.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_CONFIRMED);
            assertThat(target2.getOrderStatus()).isEqualTo(OrderStatus.PURCHASE_CONFIRMED);
            assertThat(target1.getPurchaseConfirmedAt()).isNotNull();
            assertThat(target2.getPurchaseConfirmedAt()).isNotNull();
        }

        @DisplayName("자동확정 대상이 없으면 0건을 반환한다")
        @Test
        void givenNoTargets_whenAutoConfirm_thenReturnsZero() {
            // given
            given(orderItemRepository.findAutoConfirmTargets(org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of());

            // when
            int confirmedCount = sut.autoConfirmExpired();

            // then
            assertThat(confirmedCount).isZero();
        }
    }
}
