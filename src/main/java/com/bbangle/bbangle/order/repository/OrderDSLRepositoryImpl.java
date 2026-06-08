package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.QOrder;
import com.bbangle.bbangle.order.domain.QOrderDelivery;
import com.bbangle.bbangle.order.domain.QOrderItem;
import com.bbangle.bbangle.order.domain.model.CompletedOrderSearchType;
import com.bbangle.bbangle.order.domain.model.CompletedOrderStatus;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.domain.model.OrderStatus;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.CompletedOrderSearchCommand;
import com.bbangle.bbangle.seller.domain.QSeller;
import com.bbangle.bbangle.store.domain.QStore;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class OrderDSLRepositoryImpl implements OrderDSLRepository {

    // 완료 주문에 해당하는 전체 OrderStatus 집합 (구매확정, 취소, 반품, 교환)
    private static final Set<OrderStatus> ALL_COMPLETED_STATUSES;

    static {
        Set<OrderStatus> statuses = new HashSet<>();
        statuses.add(OrderStatus.PURCHASE_CONFIRMED);
        statuses.addAll(OrderStatus.CANCELLED_GROUP);
        statuses.addAll(OrderStatus.RETURNED_GROUP);
        statuses.addAll(OrderStatus.EXCHANGED_GROUP);
        ALL_COMPLETED_STATUSES = Collections.unmodifiableSet(statuses);
    }

    private final JPAQueryFactory queryFactory;
    private final QOrder order = QOrder.order;
    private final QOrderItem orderItem = QOrderItem.orderItem;
    private final QSeller seller = QSeller.seller;
    private final QStore store = QStore.store;
    private final QProduct product = QProduct.product;
    private final QOrderDelivery orderDelivery = QOrderDelivery.orderDelivery;

    @Override
    public BbanglePageResponse<Order> searchOrderList(OrderSearchCommand command) {
        Pageable pageable = command.page();

        // 1단계: Order ID만 조회 (페이징 적용)
        List<Long> orderIds = fetchOrderIds(command, pageable);

        // 2단계: Order 엔티티 fetchJoin 조회
        List<Order> orders = fetchOrdersWithDetails(orderIds);

        // 3단계: count 쿼리
        Long total = fetchTotalCount(command);

        return BbanglePageResponse.of(new PageImpl<>(orders, pageable, total));
    }

    @Override
    public Map<OrderStatus, Long> countByOrderStatus(OrderSearchCommand command) {
        CompletedOrderSearchType searchType = command.searchType();
        String keyword = command.searchCondition() != null
            ? command.searchCondition().getKeyword()
            : null;

        // select/get에 동일 인스턴스를 사용해 Tuple 매핑이 올바르게 동작하도록 보장
        var countExpr = orderItem.id.countDistinct();

        JPAQuery<Tuple> countQuery = queryFactory
            .select(orderItem.orderStatus, countExpr)
            .from(order)
            .leftJoin(order.seller, seller)
            .leftJoin(order.orderItems, orderItem);

        addKeywordJoins(countQuery, searchType, keyword);

        // 상태별 카운트는 orderDeliveryStatus 필터 없이 전체 집계합니다.
        // 목록 쿼리(searchOrderList)와 달리 탭별 전체 건수를 보여줘야 하므로 의도적으로 다릅니다.
        // countDistinct: TRACKING_NUMBER 검색 시 orderDelivery join으로 같은 orderItem이 중복 집계되는 것을 방지합니다.
        List<Tuple> results = countQuery
            .where(
                seller.id.eq(command.sellerId()),
                dateRangePredicate(
                    extractStartDate(command),
                    extractEndDate(command)),
                keywordPredicate(searchType, keyword))
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

    @Override
    public BbanglePageResponse<Order> searchCompletedOrderList(CompletedOrderSearchCommand command) {
        Pageable pageable = command.pageable();

        List<Long> orderIds = fetchCompletedOrderIds(command, pageable);
        List<Order> orders = fetchOrdersWithDetails(orderIds);
        Long total = fetchCompletedTotalCount(command);

        return BbanglePageResponse.of(new PageImpl<>(orders, pageable, total));
    }

    @Override
    public Map<OrderStatus, Long> countByCompletedOrderStatus(CompletedOrderSearchCommand command) {
        CompletedOrderSearchType searchType = command.searchType();
        String keyword = command.searchValue();

        // select/get에 동일 인스턴스를 사용해 Tuple 매핑이 올바르게 동작하도록 보장
        var countExpr = orderItem.id.countDistinct();

        JPAQuery<Tuple> countQuery = queryFactory
            .select(orderItem.orderStatus, countExpr)
            .from(order)
            .leftJoin(order.seller, seller)
            .leftJoin(order.orderItems, orderItem);

        addKeywordJoins(countQuery, searchType, keyword);

        List<Tuple> results = countQuery
            .where(
                seller.id.eq(command.sellerId()),
                dateRangePredicate(
                    toStartDateTime(command.startDate()),
                    toEndDateTime(command.endDate())),
                // 탭 배지용 카운트이므로 취소/반품/교환을 포함한 전체 완료 상태를 집계
                orderItem.orderStatus.in(ALL_COMPLETED_STATUSES),
                keywordPredicate(searchType, keyword))
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

    private List<Long> fetchCompletedOrderIds(CompletedOrderSearchCommand command, Pageable pageable) {
        CompletedOrderSearchType searchType = command.searchType();
        String keyword = command.searchValue();

        JPAQuery<Long> idQuery = queryFactory
            .select(order.id)
            .distinct()
            .from(order)
            .leftJoin(order.seller, seller)
            .leftJoin(order.orderItems, orderItem);

        addKeywordJoins(idQuery, searchType, keyword);

        return idQuery
            .where(
                seller.id.eq(command.sellerId()),
                dateRangePredicate(
                    toStartDateTime(command.startDate()),
                    toEndDateTime(command.endDate())),
                completedOrderStatusPredicate(command.status()),
                keywordPredicate(searchType, keyword))
            .orderBy(order.orderDate.desc(), order.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    private Long fetchCompletedTotalCount(CompletedOrderSearchCommand command) {
        CompletedOrderSearchType searchType = command.searchType();
        String keyword = command.searchValue();

        JPAQuery<Long> countQuery = queryFactory
            .select(order.countDistinct())
            .from(order)
            .leftJoin(order.seller, seller)
            .leftJoin(order.orderItems, orderItem);

        addKeywordJoins(countQuery, searchType, keyword);

        Long total = countQuery
            .where(
                seller.id.eq(command.sellerId()),
                dateRangePredicate(
                    toStartDateTime(command.startDate()),
                    toEndDateTime(command.endDate())),
                completedOrderStatusPredicate(command.status()),
                keywordPredicate(searchType, keyword))
            .fetchOne();

        return total != null ? total : 0L;
    }

    private BooleanExpression completedOrderStatusPredicate(CompletedOrderStatus status) {
        // 완료 탭 전용 API: 취소/반품/교환은 별도 API로 조회하므로 항상 구매확정만 필터링
        // status 파라미터는 향후 확장을 위해 유지하지만 현재는 무시
        return orderItem.orderStatus.eq(OrderStatus.PURCHASE_CONFIRMED);
    }

    /**
     * [2단계 페이징 전략 - 1단계] fetchJoin과 페이징을 동시에 사용하면 카테시안 곱 문제가 발생합니다.
     * 예) Order 1개에 OrderItem이 3개이면 fetchJoin 결과는 3행인데, LIMIT 10을 적용하면
     * Order 10개가 아닌 OrderItem 10개(≈ Order 3~4개)만 가져오게 됩니다.
     * 이를 방지하기 위해 1단계에서는 fetchJoin 없이 ID만 조회하여 올바른 LIMIT을 적용하고,
     * 2단계(fetchOrdersWithDetails)에서 해당 ID 목록으로 fetchJoin 조회를 수행합니다.
     */
    private List<Long> fetchOrderIds(OrderSearchCommand command, Pageable pageable) {
        CompletedOrderSearchType searchType = command.searchType();
        String keyword = command.searchCondition() != null
            ? command.searchCondition().getKeyword()
            : null;

        JPAQuery<Long> idQuery = queryFactory
            .select(order.id)
            .distinct()
            .from(order)
            .leftJoin(order.seller, seller)
            .leftJoin(order.orderItems, orderItem);

        addKeywordJoins(idQuery, searchType, keyword);

        return idQuery
            .where(
                seller.id.eq(command.sellerId()),
                dateRangePredicate(
                    extractStartDate(command),
                    extractEndDate(command)),
                orderStatusPredicate(command.orderDeliveryStatus()),
                keywordPredicate(searchType, keyword))
            .orderBy(order.orderDate.desc(), order.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    /**
     * [2단계 페이징 전략 - 2단계] 1단계에서 구한 orderIds로 Order 엔티티를 fetchJoin 조회합니다.
     * distinct()를 사용하는 이유: leftJoin(order.orderItems).fetchJoin() 시 DB에서 Order 1개당
     * OrderItem N개가 조인되어 N개의 중복 Order 행이 반환됩니다. distinct()로 JPA 레벨 중복을 제거합니다.
     * (페이지네이션은 1단계에서 이미 처리했으므로 distinct()가 페이지 크기에 영향을 주지 않습니다)
     */
    private List<Order> fetchOrdersWithDetails(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Order> orders = queryFactory
            .selectFrom(order)
            .distinct()
            .leftJoin(order.seller, seller).fetchJoin()
            .leftJoin(order.orderItems, orderItem).fetchJoin()
            .leftJoin(order.payment).fetchJoin()
            .where(order.id.in(orderIds))
            .fetch();

        return restoreOriginalOrder(orders, orderIds);
    }

    /**
     * [정렬 순서 복원] fetchJoin 이후 DB가 반환하는 Order 결과 순서는 OrderItem 수에 따라 달라질 수 있습니다.
     * 1단계에서 orderDate DESC → id DESC 기준으로 정렬된 orderIds 순서가 실제 페이지 정렬 기준이므로,
     * fetchJoin 결과를 Map으로 변환한 뒤 orderIds 순서대로 재조립하여 원래 정렬 순서를 복원합니다.
     */
    private List<Order> restoreOriginalOrder(List<Order> orders, List<Long> orderIds) {
        Map<Long, Order> orderMap = orders.stream()
            .collect(Collectors.toMap(Order::getId, Function.identity()));

        // distinct()로 중복 제거 후 복원 — fetchOrderIds가 distinct 보장하지만 방어적으로 중복 제거
        return orderIds.stream()
            .distinct()
            .map(orderMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Long fetchTotalCount(OrderSearchCommand command) {
        CompletedOrderSearchType searchType = command.searchType();
        String keyword = command.searchCondition() != null
            ? command.searchCondition().getKeyword()
            : null;

        JPAQuery<Long> countQuery = queryFactory
            .select(order.countDistinct())
            .from(order)
            .leftJoin(order.seller, seller)
            .leftJoin(order.orderItems, orderItem);

        addKeywordJoins(countQuery, searchType, keyword);

        Long total = countQuery
            .where(
                seller.id.eq(command.sellerId()),
                dateRangePredicate(
                    extractStartDate(command),
                    extractEndDate(command)),
                orderStatusPredicate(command.orderDeliveryStatus()),
                keywordPredicate(searchType, keyword))
            .fetchOne();

        return total != null ? total : 0L;
    }

    /**
     * [동적 조인 추가] QueryDSL WHERE 절에서 특정 테이블 컬럼을 참조하려면
     * 해당 테이블이 FROM/JOIN 절에 반드시 포함되어 있어야 합니다.
     * 기본 쿼리에는 product·orderDelivery가 없으므로, 검색 타입에 따라 필요할 때만 동적으로 추가합니다.
     * 불필요한 조인을 피해 쿼리 성능을 유지합니다.
     * - PRODUCT_NAME 검색 시: orderItem → product 조인 추가
     * - TRACKING_NUMBER 검색 시: orderItem → orderDeliveries → orderDelivery 조인 추가
     */
    private void addKeywordJoins(JPAQuery<?> query, CompletedOrderSearchType searchType,
        String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        if (searchType == CompletedOrderSearchType.PRODUCT_NAME) {
            query.leftJoin(orderItem.product, product);
        } else if (searchType == CompletedOrderSearchType.TRACKING_NUMBER) {
            query.leftJoin(orderItem.orderDeliveries, orderDelivery);
        }
    }

    private BooleanExpression dateRangePredicate(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        return order.orderDate.between(startDate, endDate);
    }

    private BooleanExpression orderStatusPredicate(OrderDeliveryStatus orderDeliveryStatus) {
        if (orderDeliveryStatus == null) {
            return null;
        }
        return orderItem.orderDeliveryStatus.eq(orderDeliveryStatus);
    }

    private BooleanExpression keywordPredicate(CompletedOrderSearchType searchType, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        // searchType이 null이면 keyword가 있어도 조건 없이 전체 조회
        if (searchType == null) {
            return null;
        }

        // 검색 타입별로 서로 다른 컬럼에 LIKE 검색 적용 (대소문자 무관)
        return switch (searchType) {
            // 주문 번호로 검색 (Order.orderNumber)
            case ORDER_NUMBER -> order.orderNumber.containsIgnoreCase(keyword);
            // 구매자명으로 검색 (Order.buyerName)
            case BUYER_NAME -> order.buyerName.containsIgnoreCase(keyword);
            // 상품명으로 검색 (Product.title) — addKeywordJoins에서 product 조인이 선행 필요
            case PRODUCT_NAME -> product.title.containsIgnoreCase(keyword);
            // 운송장 번호로 검색 (Shipping.trackingNumber) — addKeywordJoins에서 orderDelivery 조인이 선행 필요
            case TRACKING_NUMBER -> orderDelivery.shipping.trackingNumber.containsIgnoreCase(keyword);
        };
    }

    private LocalDateTime extractStartDate(OrderSearchCommand command) {
        if (command.searchCondition() == null || command.searchCondition().getStartDate() == null) {
            return null;
        }
        return command.searchCondition().getStartDate().atStartOfDay();
    }

    private LocalDateTime extractEndDate(OrderSearchCommand command) {
        if (command.searchCondition() == null || command.searchCondition().getEndDate() == null) {
            return null;
        }
        return command.searchCondition().getEndDate().atTime(23, 59, 59);
    }

    // LocalDate → 해당 날짜 00:00:00 LocalDateTime 변환 (null-safe)
    private LocalDateTime toStartDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    // LocalDate → 해당 날짜 23:59:59 LocalDateTime 변환 (null-safe)
    private LocalDateTime toEndDateTime(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59) : null;
    }

    @Override
    public Optional<Order> findByIdWithFullAssociations(Long orderId) {
        List<Order> results = queryFactory
            .selectFrom(order)
            .distinct()
            .leftJoin(order.payment).fetchJoin()
            .join(order.seller, seller).fetchJoin()
            .join(seller.store, store).fetchJoin()
            .join(order.orderItems, orderItem).fetchJoin()
            .join(orderItem.product, product).fetchJoin()
            .where(order.id.eq(orderId))
            .fetch();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

}
