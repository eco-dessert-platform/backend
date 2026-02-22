package com.bbangle.bbangle.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[단위테스트] OrderItem Entity")
class OrderItemInfoUnitTest {

    @DisplayName("orderDeliveries 필드가 초기화되어 있어 NPE가 발생하지 않는다")
    @Test
    void givenNewOrderItem_whenGetOrderDeliveries_thenNotNull() {
        // given
        OrderItem orderItem = newEntity(OrderItem.class);

        // when
        List<OrderDelivery> result = orderItem.getOrderDeliveries();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @DisplayName("addOrderDelivery()는 양방향 연관관계를 자동으로 설정한다")
    @Test
    void givenOrderDelivery_whenAddOrderDelivery_thenBidirectionalRelationshipSet() {
        // given
        OrderItem orderItem = newEntity(OrderItem.class);
        OrderDelivery orderDelivery = newEntity(OrderDelivery.class);
        ReflectionTestUtils.setField(orderDelivery, "id", 1L);

        // when
        orderItem.addOrderDelivery(orderDelivery);

        // then
        assertThat(orderItem.getOrderDeliveries()).hasSize(1);
        assertThat(orderItem.getOrderDeliveries()).contains(orderDelivery);
        assertThat(orderDelivery.getOrderItem()).isEqualTo(orderItem);
    }

    @DisplayName("addOrderDeliveries()는 여러 배송 정보를 한 번에 추가하고 양방향 관계를 설정한다")
    @Test
    void givenMultipleOrderDeliveries_whenAddOrderDeliveries_thenAllItemsAdded() {
        // given
        OrderItem orderItem = newEntity(OrderItem.class);
        OrderDelivery delivery1 = newEntity(OrderDelivery.class);
        OrderDelivery delivery2 = newEntity(OrderDelivery.class);
        OrderDelivery delivery3 = newEntity(OrderDelivery.class);

        ReflectionTestUtils.setField(delivery1, "id", 1L);
        ReflectionTestUtils.setField(delivery2, "id", 2L);
        ReflectionTestUtils.setField(delivery3, "id", 3L);

        List<OrderDelivery> deliveries = List.of(delivery1, delivery2, delivery3);

        // when
        orderItem.addOrderDeliveries(deliveries);

        // then
        assertThat(orderItem.getOrderDeliveries()).hasSize(3);
        assertThat(orderItem.getOrderDeliveries()).containsExactlyInAnyOrder(delivery1, delivery2, delivery3);

        assertThat(delivery1.getOrderItem()).isEqualTo(orderItem);
        assertThat(delivery2.getOrderItem()).isEqualTo(orderItem);
        assertThat(delivery3.getOrderItem()).isEqualTo(orderItem);
    }

    @DisplayName("동일한 OrderDelivery를 여러 번 추가해도 중복으로 추가된다")
    @Test
    void givenSameOrderDelivery_whenAddMultipleTimes_thenDuplicateAdded() {
        // given
        OrderItem orderItem = newEntity(OrderItem.class);
        OrderDelivery orderDelivery = newEntity(OrderDelivery.class);
        ReflectionTestUtils.setField(orderDelivery, "id", 1L);

        // when
        orderItem.addOrderDelivery(orderDelivery);
        orderItem.addOrderDelivery(orderDelivery);

        // then
        assertThat(orderItem.getOrderDeliveries()).hasSize(2);
    }

    private <T> T newEntity(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
