package com.bbangle.bbangle.order.customer.repository;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderDetailCommand;
import com.bbangle.bbangle.order.customer.service.model.CustomerOrderCommand.CustomerOrderSearchCommand;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.QOrder;
import com.bbangle.bbangle.order.domain.QOrderItem;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.store.domain.QStore;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * 소비자 주문목록 조회 전용 QueryDSL 리포지토리.
 * 판매자 조회 로직({@code OrderDSLRepository})과 분리하여 소비자 관점 쿼리만 담습니다.
 * 인증 회원의 주문을 주문일 최신순으로 페이징 조회하며, 결제 전(PAYMENT_PENDING) 주문은 제외합니다.
 */
@Repository
@RequiredArgsConstructor
public class CustomerOrderRepository {

    private final JPAQueryFactory queryFactory;
    private final QOrder order = QOrder.order;
    private final QOrderItem orderItem = QOrderItem.orderItem;
    private final QProduct product = QProduct.product;
    private final QBoard board = QBoard.board;
    private final QStore store = QStore.store;

    /**
     * 회원의 주문을 주문일(orderDate) 최신순으로 페이징 조회합니다.
     * fetchJoin + 페이징 시 카테시안 곱 문제를 피하기 위해 2단계로 조회합니다.
     * 1) ID만 페이징 조회 → 2) 해당 ID로 orderItems/product/board/payment fetchJoin 조회 후 정렬 순서 복원
     */
    public BbanglePageResponse<Order> searchCustomerOrderList(CustomerOrderSearchCommand command) {
        Pageable pageable = command.pageable();

        List<Long> orderIds = fetchCustomerOrderIds(command.memberId(), pageable);
        List<Order> orders = fetchOrdersWithDetails(orderIds);
        Long total = fetchCustomerTotalCount(command.memberId());

        return BbanglePageResponse.of(new PageImpl<>(orders, pageable, total));
    }

    /**
     * 회원 소유의 단일 주문 상세를 조회합니다. (소유권 검증 포함)
     *
     * <p>단일 컬렉션({@code orderItems})만 fetchJoin 하고 나머지는 to-one 연관이므로
     * 카테시안 곱 없이 한 번의 쿼리로 주문상품/상품/게시글/스토어/결제를 로딩합니다.
     * 배송정보(OrderDelivery)는 주문상품 1:N 컬렉션이라 함께 fetchJoin 하지 않고
     * 서비스 계층에서 {@code findLatestByOrderItemIds} 로 별도 일괄 로딩하여 N+1 을 방지합니다.
     */
    public Optional<Order> findCustomerOrderDetail(CustomerOrderDetailCommand command) {
        Order result = queryFactory
            .selectFrom(order)
            .distinct()
            .leftJoin(order.orderItems, orderItem).fetchJoin()
            .leftJoin(orderItem.product, product).fetchJoin()
            .leftJoin(product.board, board).fetchJoin()
            .leftJoin(product.store, store).fetchJoin()
            .leftJoin(order.payment).fetchJoin()
            .where(
                order.id.eq(command.orderId()),
                order.member.id.eq(command.memberId())
            )
            .fetchOne();

        return Optional.ofNullable(result);
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
            // 집계 쿼리라 offset/limit 이 없으므로 서브쿼리 없이 상태를 직접 걸러도 안전하다.
            .where(
                order.member.id.eq(command.memberId()),
                orderItem.orderStatus.ne(OrderStatus.PAYMENT_PENDING)
            )
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
            .where(order.member.id.eq(memberId), hasPaidOrderItem())
            .orderBy(order.orderDate.desc(), order.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    /**
     * 결제 전(PAYMENT_PENDING) 주문을 소비자 주문목록에서 제외하기 위한 조건입니다.
     *
     * <p>주문 생성 시 모든 주문상품이 PAYMENT_PENDING 이고 결제 승인 시 전부 전환되므로,
     * "결제완료된 주문상품이 하나라도 있는 주문"은 곧 "결제가 끝난 주문"과 같습니다.
     *
     * <p>orderItems 를 조인해 {@code ne()} 로 거르지 않는 이유는, Order↔OrderItem 이 1:N 이라
     * 조인 후에는 offset/limit 이 조인 결과 행 기준으로 동작해 페이지 크기가 어긋나기 때문입니다.
     * EXISTS 서브쿼리는 행을 늘리지 않아 페이징에 영향을 주지 않습니다.
     */
    private BooleanExpression hasPaidOrderItem() {
        QOrderItem paidItem = new QOrderItem("paidOrderItem");
        return JPAExpressions
            .selectOne()
            .from(paidItem)
            .where(
                paidItem.order.eq(order),
                paidItem.orderStatus.ne(OrderStatus.PAYMENT_PENDING)
            )
            .exists();
    }

    private Long fetchCustomerTotalCount(Long memberId) {
        Long total = queryFactory
            .select(order.count())
            .from(order)
            // 목록 조회와 동일한 조건을 걸어야 총 건수와 실제 노출 건수가 어긋나지 않는다.
            .where(order.member.id.eq(memberId), hasPaidOrderItem())
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
            .leftJoin(orderItem.product, product).fetchJoin()
            .leftJoin(product.board, board).fetchJoin()
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
