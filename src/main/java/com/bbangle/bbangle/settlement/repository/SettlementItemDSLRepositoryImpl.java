package com.bbangle.bbangle.settlement.repository;

import com.bbangle.bbangle.order.domain.QOrder;
import com.bbangle.bbangle.order.domain.QOrderItem;
import com.bbangle.bbangle.board.domain.QProduct;
import com.bbangle.bbangle.settlement.domain.QSettlementItem;
import com.bbangle.bbangle.settlement.domain.model.SettlementItemDateType;
import com.bbangle.bbangle.settlement.domain.model.SettlementItemSearchType;
import com.bbangle.bbangle.settlement.repository.dao.SettlementItemDetailDao;
import com.bbangle.bbangle.settlement.repository.dao.SettlementItemSummaryDao;
import com.bbangle.bbangle.settlement.seller.excel.service.model.SettlementItemExcelSearchCommand;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.SettlementItemSearchCommand;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.Expressions;
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
                dateGoe(command.startDate(), command.dateType()),
                dateLoe(command.endDate(), command.dateType()),
                searchValueEq(command.searchType(), command.searchValue())
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
            .leftJoin(settlementItem.orderItem, orderItem)
            .leftJoin(orderItem.order, order)
            .where(
                sellerEq(command.sellerId()),
                dateGoe(command.startDate(), command.dateType()),
                dateLoe(command.endDate(), command.dateType()),
                searchValueEq(command.searchType(), command.searchValue())
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
            .leftJoin(settlementItem.orderItem, orderItem)
            .leftJoin(orderItem.order, order)
            .where(
                sellerEq(command.sellerId()),
                dateGoe(command.startDate(), command.dateType()),
                dateLoe(command.endDate(), command.dateType()),
                searchValueEq(command.searchType(), command.searchValue())
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
     * baseDate 조회 시작일 조건 (null이면 하한 없음). 엑셀 다운로드(SettlementItemExcelSearchCommand) 전용.
     */
    private BooleanExpression baseDateGoe(LocalDate startDate) {
        return startDate != null ? settlementItem.baseDate.goe(startDate) : null;
    }

    /**
     * baseDate 조회 종료일 조건 (null이면 상한 없음). 엑셀 다운로드(SettlementItemExcelSearchCommand) 전용.
     */
    private BooleanExpression baseDateLoe(LocalDate endDate) {
        return endDate != null ? settlementItem.baseDate.loe(endDate) : null;
    }

    /**
     * dateType 기준 조회 시작일 조건 (null이면 하한 없음).
     */
    private BooleanExpression dateGoe(LocalDate startDate, SettlementItemDateType dateType) {
        return startDate != null ? dateTypePath(dateType).goe(startDate) : null;
    }

    /**
     * dateType 기준 조회 종료일 조건 (null이면 상한 없음).
     */
    private BooleanExpression dateLoe(LocalDate endDate, SettlementItemDateType dateType) {
        return endDate != null ? dateTypePath(dateType).loe(endDate) : null;
    }

    /**
     * dateType에 대응하는 SettlementItem의 날짜 컬럼 경로를 반환한다.
     * dateType이 null이면(기존 API 호환) baseDate를 기본으로 사용한다.
     */
    private DatePath<LocalDate> dateTypePath(SettlementItemDateType dateType) {
        if (dateType == SettlementItemDateType.SCHEDULED_DATE) {
            return settlementItem.scheduledDate;
        }
        if (dateType == SettlementItemDateType.COMPLETED_DATE) {
            return settlementItem.completedDate;
        }
        return settlementItem.baseDate;
    }

    /**
     * 검색 구분(searchType)에 따른 검색어 조건.
     * searchType 또는 searchValue가 없으면(blank 포함) 조건 없이 전체 조회한다.
     * - ORDER_NUMBER: 주문번호 부분 일치(대소문자 무관) — CompletedOrderSearchType.ORDER_NUMBER와 동일한 방식
     * - ORDER_ITEM_ID: 상품주문번호(OrderItem PK) 완전 일치 — 식별자 성격의 숫자 PK이므로 exact match
     */
    private BooleanExpression searchValueEq(SettlementItemSearchType searchType, String searchValue) {
        if (searchType == null || searchValue == null || searchValue.isBlank()) {
            return null;
        }
        return switch (searchType) {
            case ORDER_NUMBER -> order.orderNumber.containsIgnoreCase(searchValue);
            case ORDER_ITEM_ID -> orderItemIdEq(searchValue);
        };
    }

    /**
     * 검색어를 OrderItem PK(Long)로 파싱하여 완전 일치 조건을 만든다.
     * 숫자로 파싱할 수 없는 검색어는 결과 없음으로 처리한다 (BoardDetailRepositoryImpl과 동일한 방식).
     */
    private BooleanExpression orderItemIdEq(String searchValue) {
        try {
            return orderItem.id.eq(Long.parseLong(searchValue.trim()));
        } catch (NumberFormatException e) {
            return Expressions.asBoolean(false);
        }
    }

}
