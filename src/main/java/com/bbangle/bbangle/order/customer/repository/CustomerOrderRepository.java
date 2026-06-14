package com.bbangle.bbangle.order.customer.repository;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderSearchCommand;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.QOrder;
import com.bbangle.bbangle.order.domain.QOrderItem;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * 소비자 주문목록 조회 전용 QueryDSL 리포지토리.
 * 판매자 조회 로직({@code OrderDSLRepository})과 분리하여 소비자 관점 쿼리만 담습니다.
 * 현재는 별도 필터 없이 인증 회원의 주문을 주문일 최신순으로 페이징 조회합니다.
 */
@Repository
@RequiredArgsConstructor
public class CustomerOrderRepository {

    private final JPAQueryFactory queryFactory;
    private final QOrder order = QOrder.order;
    private final QOrderItem orderItem = QOrderItem.orderItem;

    /**
     * 회원의 주문을 주문일(orderDate) 최신순으로 페이징 조회합니다.
     * fetchJoin + 페이징 시 카테시안 곱 문제를 피하기 위해 2단계로 조회합니다.
     * 1) ID만 페이징 조회 → 2) 해당 ID로 orderItems/payment fetchJoin 조회 후 정렬 순서 복원
     */
    public BbanglePageResponse<Order> searchCustomerOrderList(CustomerOrderSearchCommand command) {
        Pageable pageable = command.pageable();

        List<Long> orderIds = fetchCustomerOrderIds(command.memberId(), pageable);
        List<Order> orders = fetchOrdersWithDetails(orderIds);
        Long total = fetchCustomerTotalCount(command.memberId());

        return BbanglePageResponse.of(new PageImpl<>(orders, pageable, total));
    }

    /**
     * 회원의 주문을 OrderStatus 별로 집계합니다. (탭 배지용 카운트)
     */
    public Map<OrderStatus, Long> countByCustomerOrderStatus(CustomerOrderSearchCommand command) {
        var countExpr = orderItem.id.countDistinct();

        List<Tuple> results = queryFactory
            .select(orderItem.orderStatus, countExpr)
            .from(order)
            .leftJoin(order.orderItems, orderItem)
            .where(order.member.id.eq(command.memberId()))
            .groupBy(orderItem.orderStatus)
            .fetch();

        Map<OrderStatus, Long> countMap = new EnumMap<>(OrderStatus.class);
        for (Tuple tuple : results) {
            OrderStatus status = tuple.get(orderItem.orderStatus);
            Long count = tuple.get(countExpr);
            if (status != null && count != null) {
                countMap.put(status, count);
            }
        }
        return countMap;
    }

    private List<Long> fetchCustomerOrderIds(Long memberId, Pageable pageable) {
        return queryFactory
            .select(order.id)
            .from(order)
            .where(order.member.id.eq(memberId))
            .orderBy(order.orderDate.desc(), order.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    private Long fetchCustomerTotalCount(Long memberId) {
        Long total = queryFactory
            .select(order.count())
            .from(order)
            .where(order.member.id.eq(memberId))
            .fetchOne();

        return total != null ? total : 0L;
    }

    private List<Order> fetchOrdersWithDetails(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Order> orders = queryFactory
            .selectFrom(order)
            .distinct()
            .leftJoin(order.orderItems, orderItem).fetchJoin()
            .leftJoin(order.payment).fetchJoin()
            .where(order.id.in(orderIds))
            .fetch();

        return restoreOriginalOrder(orders, orderIds);
    }

    // fetchJoin 이후 흐트러진 결과 순서를 1단계 orderIds 정렬 순서로 복원합니다.
    private List<Order> restoreOriginalOrder(List<Order> orders, List<Long> orderIds) {
        Map<Long, Order> orderMap = orders.stream()
            .collect(Collectors.toMap(Order::getId, Function.identity()));

        return orderIds.stream()
            .distinct()
            .map(orderMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
