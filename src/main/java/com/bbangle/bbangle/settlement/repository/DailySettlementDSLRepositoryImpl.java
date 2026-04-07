package com.bbangle.bbangle.settlement.repository;

import com.bbangle.bbangle.settlement.domain.DailySettlement;
import com.bbangle.bbangle.settlement.domain.QDailySettlement;
import com.bbangle.bbangle.settlement.repository.dao.DailySettlementSummaryDao;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.DailySettlementSearchCommand;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DailySettlementDSLRepositoryImpl implements DailySettlementDSLRepository {

    private final JPAQueryFactory queryFactory;

    private static final QDailySettlement dailySettlement = QDailySettlement.dailySettlement;

    @Override
    public Page<DailySettlement> searchDailySettlements(DailySettlementSearchCommand command) {
        Pageable pageable = command.pageable();

        // 페이징 목록 조회 (OneToMany 없으므로 단순 offset/limit)
        List<DailySettlement> content = queryFactory
            .selectFrom(dailySettlement)
            .where(
                sellerEq(command.sellerId()),
                scheduledDateGoe(command.startDate()),
                scheduledDateLoe(command.endDate())
            )
            .orderBy(dailySettlement.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long totalCount = fetchTotalCount(command);

        return new PageImpl<>(content, pageable, totalCount);
    }

    @Override
    public DailySettlementSummaryDao fetchSummary(DailySettlementSearchCommand command) {
        // 정산금액 합계 표현식 (DailySettlement.getTotalSettlementAmount()와 동일한 공식)
        NumberExpression<BigDecimal> totalAmountExpr =
            dailySettlement.amount.coalesce(BigDecimal.ZERO)
                .add(dailySettlement.fee.coalesce(BigDecimal.ZERO))
                .add(dailySettlement.deductibleRefund.coalesce(BigDecimal.ZERO))
                .add(dailySettlement.withHoldingPayment.coalesce(BigDecimal.ZERO))
                .sum();

        Tuple result = queryFactory
            .select(
                dailySettlement.scheduledDate.min(),
                dailySettlement.scheduledDate.max(),
                totalAmountExpr
            )
            .from(dailySettlement)
            .where(
                sellerEq(command.sellerId()),
                scheduledDateGoe(command.startDate()),
                scheduledDateLoe(command.endDate())
            )
            .fetchOne();

        if (result == null) {
            return new DailySettlementSummaryDao(null, null, BigDecimal.ZERO);
        }

        BigDecimal totalAmount = result.get(totalAmountExpr);

        return new DailySettlementSummaryDao(
            result.get(dailySettlement.scheduledDate.min()),
            result.get(dailySettlement.scheduledDate.max()),
            totalAmount != null ? totalAmount : BigDecimal.ZERO
        );
    }

    private long fetchTotalCount(DailySettlementSearchCommand command) {
        Long count = queryFactory
            .select(dailySettlement.count())
            .from(dailySettlement)
            .where(
                sellerEq(command.sellerId()),
                scheduledDateGoe(command.startDate()),
                scheduledDateLoe(command.endDate())
            )
            .fetchOne();

        return count != null ? count : 0L;
    }

    private BooleanExpression sellerEq(Long sellerId) {
        return dailySettlement.seller.id.eq(sellerId);
    }

    private BooleanExpression scheduledDateGoe(LocalDate startDate) {
        return startDate != null ? dailySettlement.scheduledDate.goe(startDate) : null;
    }

    private BooleanExpression scheduledDateLoe(LocalDate endDate) {
        return endDate != null ? dailySettlement.scheduledDate.loe(endDate) : null;
    }

}
