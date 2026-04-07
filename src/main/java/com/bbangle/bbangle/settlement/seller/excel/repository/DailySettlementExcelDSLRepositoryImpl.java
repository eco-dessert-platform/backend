package com.bbangle.bbangle.settlement.seller.excel.repository;

import com.bbangle.bbangle.settlement.domain.DailySettlement;
import com.bbangle.bbangle.settlement.domain.QDailySettlement;
import com.bbangle.bbangle.settlement.seller.excel.service.model.DailySettlementExcelSearchCommand;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 엑셀 다운로드 전용 QueryDSL 구현체.
 * 기존 DailySettlementDSLRepositoryImpl과 독립적으로 운영되므로
 * 같은 BooleanExpression 헬퍼 메서드를 이 클래스에 직접 정의한다.
 */
@Repository
@RequiredArgsConstructor
public class DailySettlementExcelDSLRepositoryImpl implements DailySettlementExcelDSLRepository {

    private final JPAQueryFactory queryFactory;

    private static final QDailySettlement dailySettlement = QDailySettlement.dailySettlement;

    @Override
    public List<DailySettlement> findAllForExcel(DailySettlementExcelSearchCommand command) {
        // 페이지네이션 없이 전체 조회 (엑셀 다운로드용)
        return queryFactory
            .selectFrom(dailySettlement)
            .where(
                sellerEq(command.sellerId()),
                scheduledDateGoe(command.startDate()),
                scheduledDateLoe(command.endDate())
            )
            .orderBy(dailySettlement.id.desc())
            .fetch();
    }

    private BooleanExpression sellerEq(Long sellerId) {
        return dailySettlement.seller.id.eq(sellerId);
    }

    private BooleanExpression scheduledDateGoe(LocalDate startDate) {
        // startDate가 null이면 조건 없음 (하한 없이 전체 조회)
        return startDate != null ? dailySettlement.scheduledDate.goe(startDate) : null;
    }

    private BooleanExpression scheduledDateLoe(LocalDate endDate) {
        // endDate가 null이면 조건 없음 (상한 없이 전체 조회)
        return endDate != null ? dailySettlement.scheduledDate.loe(endDate) : null;
    }

}
