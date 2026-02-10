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
import java.util.List;
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
        CompletedOrderSearchType searchType = command.searchType();
        String keyword = command.searchCondition() != null
            ? command.searchCondition().getKeyword()
            : null;

        JPAQuery<Order> dataQuery = queryFactory
            .selectFrom(order)
            .distinct()
            .leftJoin(order.seller, seller).fetchJoin()
            .leftJoin(order.orderItems, orderItem).fetchJoin()
            .leftJoin(order.payment).fetchJoin();

        addKeywordJoins(dataQuery, searchType, keyword);

        List<Order> orders = dataQuery
            .where(
                seller.id.eq(command.sellerId()),
                dateRangePredicate(
                    command.searchCondition().getStartDate().atStartOfDay(),
                    command.searchCondition().getEndDate().atTime(23, 59, 59)),
                orderStatusPredicate(command.orderDeliveryStatus()),
                keywordPredicate(searchType, keyword))
            .orderBy(order.orderDate.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(order.countDistinct())
            .from(order)
            .leftJoin(order.seller, seller)
            .leftJoin(order.orderItems, orderItem)
            .leftJoin(order.payment);

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

        return BbanglePageResponse.of(new PageImpl<>(orders, pageable, total != null ? total : 0L));
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
