package com.bbangle.bbangle.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("[도메인] Order 엔티티")
class OrderUnitTest {

    @DisplayName("orderItems 필드가 초기화되어 있어 NPE가 발생하지 않는다")
    @Test
    void givenNewOrder_whenGetOrderItems_thenNotNull() {
        // given
        Order order = newEntity(Order.class);

        // when
        List<OrderItem> result = order.getOrderItems();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @DisplayName("addOrderItem()은 양방향 연관관계를 자동으로 설정한다")
    @Test
    void givenOrderItem_whenAddOrderItem_thenBidirectionalRelationshipSet() {
        // given
        Order order = newEntity(Order.class);
        OrderItem orderItem = newEntity(OrderItem.class);
        ReflectionTestUtils.setField(orderItem, "id", 1L);

        // when
        order.addOrderItem(orderItem);

        // then
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getOrderItems()).contains(orderItem);
        assertThat(orderItem.getOrder()).isEqualTo(order);
    }

    @DisplayName("addOrderItems()는 여러 주문 항목을 한 번에 추가하고 양방향 관계를 설정한다")
    @Test
    void givenMultipleOrderItems_whenAddOrderItems_thenAllItemsAdded() {
        // given
        Order order = newEntity(Order.class);
        OrderItem item1 = newEntity(OrderItem.class);
        OrderItem item2 = newEntity(OrderItem.class);
        OrderItem item3 = newEntity(OrderItem.class);

        ReflectionTestUtils.setField(item1, "id", 1L);
        ReflectionTestUtils.setField(item2, "id", 2L);
        ReflectionTestUtils.setField(item3, "id", 3L);

        List<OrderItem> items = List.of(item1, item2, item3);

        // when
        order.addOrderItems(items);

        // then
        assertThat(order.getOrderItems()).hasSize(3);
        assertThat(order.getOrderItems()).containsExactlyInAnyOrder(item1, item2, item3);

        assertThat(item1.getOrder()).isEqualTo(order);
        assertThat(item2.getOrder()).isEqualTo(order);
        assertThat(item3.getOrder()).isEqualTo(order);
    }

    @DisplayName("동일한 OrderItem을 여러 번 추가해도 중복으로 추가된다")
    @Test
    void givenSameOrderItem_whenAddMultipleTimes_thenDuplicateAdded() {
        // given
        Order order = newEntity(Order.class);
        OrderItem orderItem = newEntity(OrderItem.class);
        ReflectionTestUtils.setField(orderItem, "id", 1L);

        // when
        order.addOrderItem(orderItem);
        order.addOrderItem(orderItem);

        // then
        assertThat(order.getOrderItems()).hasSize(2);
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
