package com.bbangle.bbangle.settlement.repository;

import com.bbangle.bbangle.order.domain.QOrder;
import com.bbangle.bbangle.order.domain.QOrderItem;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.settlement.domain.QSettlementItem;
import com.bbangle.bbangle.settlement.repository.dao.SettlementItemDetailDao;
import com.bbangle.bbangle.settlement.repository.dao.SettlementItemSummaryDao;
import com.bbangle.bbangle.settlement.seller.excel.service.model.SettlementItemExcelSearchCommand;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.SettlementItemSearchCommand;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * 건별 정산 내역 QueryDSL 구현체.
 * SettlementItem → OrderItem → Order, Product 의 조인을 통해
 * 주문 정보 및 상품 정보를 DTO로 직접 프로젝션하여 N+1 문제를 방지한다.
 *
 * Spring Data JPA 커스텀 구현체 네이밍 규칙에 따라 @Repository 어노테이션 없이 자동 인식된다.
 * (SettlementItemRepository + SettlementItemDSLRepository 상속 구조를 통해 감지)
 */
@RequiredArgsConstructor
public class SettlementItemDSLRepositoryImpl implements SettlementItemDSLRepository {

    private final JPAQueryFactory queryFactory;

    // 쿼리 타입 정적 인스턴스
    private static final QSettlementItem settlementItem = QSettlementItem.settlementItem;
    private static final QOrderItem orderItem = QOrderItem.orderItem;
    private static final QOrder order = QOrder.order;
    private static final QProduct product = QProduct.product;

    @Override
    public Page<SettlementItemDetailDao> searchSettlementItems(SettlementItemSearchCommand command) {
        Pageable pageable = command.pageable();

        // 조인을 통해 DTO로 직접 프로젝션 — sellerId는 dailySettlement 경로를 통해 필터링
        List<SettlementItemDetailDao> content = queryFactory
            .select(Projections.constructor(SettlementItemDetailDao.class,
                order.orderNumber,
                orderItem.id,
                settlementItem.dailySettlement.seller.id,
                order.buyerName,
                product.title,
                settlementItem.scheduledAmount,
                orderItem.quantity,
                settlementItem.baseDate,
                settlementItem.scheduledDate,
                settlementItem.status
            ))
            .from(settlementItem)
            .leftJoin(settlementItem.orderItem, orderItem)
            .leftJoin(orderItem.order, order)
            .leftJoin(orderItem.product, product)
            .where(
                sellerEq(command.sellerId()),
                baseDateGoe(command.startDate()),
                baseDateLoe(command.endDate())
            )
            .orderBy(settlementItem.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long totalCount = fetchTotalCount(command);

        return new PageImpl<>(content, pageable, totalCount);
    }

    @Override
    public SettlementItemSummaryDao fetchSummary(SettlementItemSearchCommand command) {
        // 총 건수는 페이징 쿼리에서 재활용하므로 금액 합계만 조회한다.
        BigDecimal totalAmount = queryFactory
            .select(settlementItem.scheduledAmount.coalesce(BigDecimal.ZERO).sum())
            .from(settlementItem)
            .where(
                sellerEq(command.sellerId()),
                baseDateGoe(command.startDate()),
                baseDateLoe(command.endDate())
            )
            .fetchOne();

        return new SettlementItemSummaryDao(
            totalAmount != null ? totalAmount : BigDecimal.ZERO
        );
    }

    @Override
    public List<SettlementItemDetailDao> findAllForExcel(SettlementItemExcelSearchCommand command) {
        // 엑셀 다운로드: 페이지네이션 없이 전체 조회
        return queryFactory
            .select(Projections.constructor(SettlementItemDetailDao.class,
                order.orderNumber,
                orderItem.id,
                settlementItem.dailySettlement.seller.id,
                order.buyerName,
                product.title,
                settlementItem.scheduledAmount,
                orderItem.quantity,
                settlementItem.baseDate,
                settlementItem.scheduledDate,
                settlementItem.status
            ))
            .from(settlementItem)
            .leftJoin(settlementItem.orderItem, orderItem)
            .leftJoin(orderItem.order, order)
            .leftJoin(orderItem.product, product)
            .where(
                sellerEq(command.sellerId()),
                baseDateGoe(command.startDate()),
                baseDateLoe(command.endDate())
            )
            .orderBy(settlementItem.id.desc())
            .fetch();
    }

    private long fetchTotalCount(SettlementItemSearchCommand command) {
        Long count = queryFactory
            .select(settlementItem.count())
            .from(settlementItem)
            .where(
                sellerEq(command.sellerId()),
                baseDateGoe(command.startDate()),
                baseDateLoe(command.endDate())
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    /**
     * 판매자 ID 일치 조건.
     * dailySettlement.seller.id 경로를 통해 필터링한다.
     */
    private BooleanExpression sellerEq(Long sellerId) {
        return settlementItem.dailySettlement.seller.id.eq(sellerId);
    }

    /**
     * baseDate 조회 시작일 조건 (null이면 하한 없음).
     */
    private BooleanExpression baseDateGoe(LocalDate startDate) {
        return startDate != null ? settlementItem.baseDate.goe(startDate) : null;
    }

    /**
     * baseDate 조회 종료일 조건 (null이면 상한 없음).
     */
    private BooleanExpression baseDateLoe(LocalDate endDate) {
        return endDate != null ? settlementItem.baseDate.loe(endDate) : null;
    }

}
