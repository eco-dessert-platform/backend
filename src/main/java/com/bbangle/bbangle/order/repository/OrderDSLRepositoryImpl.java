package com.bbangle.bbangle.order.repository;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.order.domain.Order;
import com.bbangle.bbangle.order.domain.QOrder;
import com.bbangle.bbangle.order.domain.QOrderItem;
import com.bbangle.bbangle.order.domain.model.OrderDeliveryStatus;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.OrderSearchCommand;
import com.bbangle.bbangle.seller.domain.QSeller;
import com.querydsl.core.types.dsl.BooleanExpression;
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

    @Override
    public BbanglePageResponse<Order> searchOrderList(OrderSearchCommand command) {
        Pageable pageable = command.page();

        List<Order> orders = queryFactory
            .selectFrom(order)
            .distinct()
            .leftJoin(order.seller, seller).fetchJoin()
            .leftJoin(order.orderItems, orderItem).fetchJoin()
            .leftJoin(order.payment).fetchJoin()
            .where(
                seller.id.eq(command.sellerId()),
                dateRangePredicate(
                    command.searchCondition().getStartDate().atStartOfDay(),
                    command.searchCondition().getEndDate().atTime(23, 59, 59)),
                orderStatusPredicate(command.orderDeliveryStatus()),
                keywordPredicate(command.searchCondition().getKeyword()))
            .orderBy(order.orderDate.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(order.countDistinct())
            .from(order)
            .leftJoin(order.seller, seller)
            .leftJoin(order.orderItems, orderItem)
            .leftJoin(order.payment)
            .where(
                seller.id.eq(command.sellerId()),
                dateRangePredicate(
                    command.searchCondition().getStartDate().atStartOfDay(),
                    command.searchCondition().getEndDate().atTime(23, 59, 59)),
                orderStatusPredicate(command.orderDeliveryStatus()),
                keywordPredicate(command.searchCondition().getKeyword()))
            .fetchOne();

        return BbanglePageResponse.of(new PageImpl<>(orders, pageable, total != null ? total : 0L));
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

    private BooleanExpression keywordPredicate(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return order.orderNumber.containsIgnoreCase(keyword)
            .or(order.buyerName.containsIgnoreCase(keyword));
    }

}
