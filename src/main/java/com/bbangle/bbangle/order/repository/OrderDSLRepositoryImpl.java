package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.QOrder;
import com.bbangle.bbangle.order.domain.QOrderDelivery;
import com.bbangle.bbangle.order.domain.QOrderItem;
import com.bbangle.bbangle.order.domain.model.CompletedOrderSearchType;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import com.bbangle.bbangle.seller.domain.QSeller;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.Collections;
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

    private final JPAQueryFactory queryFactory;
    private final QOrder order = QOrder.order;
    private final QOrderItem orderItem = QOrderItem.orderItem;
    private final QSeller seller = QSeller.seller;
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

    private List<Long> fetchOrderIds(OrderSearchCommand command, Pageable pageable) {
        CompletedOrderSearchType searchType = command.searchType();
        String keyword = command.searchCondition() != null
            ? command.searchCondition().getKeyword()
            : null;

        JPAQuery<Long> idQuery = queryFactory
            .select(order.id)
            .from(order)
            .leftJoin(order.seller, seller)
            .leftJoin(order.orderItems, orderItem);

        addKeywordJoins(idQuery, searchType, keyword);

        return idQuery
            .where(
                seller.id.eq(command.sellerId()),
                dateRangePredicate(
                    command.searchCondition().getStartDate().atStartOfDay(),
                    command.searchCondition().getEndDate().atTime(23, 59, 59)),
                orderStatusPredicate(command.orderDeliveryStatus()),
                keywordPredicate(searchType, keyword))
            .orderBy(order.orderDate.desc(), order.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

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

    private List<Order> restoreOriginalOrder(List<Order> orders, List<Long> orderIds) {
        Map<Long, Order> orderMap = orders.stream()
            .collect(Collectors.toMap(Order::getId, Function.identity()));

        return orderIds.stream()
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
                    command.searchCondition().getStartDate().atStartOfDay(),
                    command.searchCondition().getEndDate().atTime(23, 59, 59)),
                orderStatusPredicate(command.orderDeliveryStatus()),
                keywordPredicate(searchType, keyword))
            .fetchOne();

        return total != null ? total : 0L;
    }

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

        return switch (searchType) {
            case ORDER_NUMBER -> order.orderNumber.containsIgnoreCase(keyword);
            case BUYER_NAME -> order.buyerName.containsIgnoreCase(keyword);
            case PRODUCT_NAME -> product.title.containsIgnoreCase(keyword);
            case TRACKING_NUMBER -> orderDelivery.shipping.trackingNumber.containsIgnoreCase(keyword);
        };
    }

}
