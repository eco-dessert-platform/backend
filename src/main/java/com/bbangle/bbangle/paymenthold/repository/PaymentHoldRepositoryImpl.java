package com.bbangle.bbangle.paymenthold.repository;

import com.bbangle.bbangle.paymenthold.domain.PaymentHold;
import com.bbangle.bbangle.paymenthold.domain.QPaymentHold;
import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldDateType;
import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldStatus;
import com.bbangle.bbangle.paymenthold.seller.excel.service.model.PaymentHoldExcelSearchCommand;
import com.bbangle.bbangle.paymenthold.seller.service.model.PaymentHoldSearchCommand;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class PaymentHoldRepositoryImpl implements PaymentHoldDSLRepository {

    private static final QPaymentHold paymentHold = QPaymentHold.paymentHold;

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PaymentHold> searchPaymentHolds(PaymentHoldSearchCommand command) {
        Pageable pageable = command.pageable();

        List<PaymentHold> content = queryFactory
            .selectFrom(paymentHold)
            .where(
                sellerEq(command.sellerId()),
                paymentHoldIdEq(command.paymentHoldId()),
                settlementNumberEq(command.settlementId()),
                statusEq(command.status()),
                dateGoe(command.startDate(), command.hasIdSearch(), command.dateType()),
                dateLoe(command.endDate(), command.hasIdSearch(), command.dateType())
            )
            .orderBy(paymentHold.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(paymentHold.count())
            .from(paymentHold)
            .where(
                sellerEq(command.sellerId()),
                paymentHoldIdEq(command.paymentHoldId()),
                settlementNumberEq(command.settlementId()),
                statusEq(command.status()),
                dateGoe(command.startDate(), command.hasIdSearch(), command.dateType()),
                dateLoe(command.endDate(), command.hasIdSearch(), command.dateType())
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<PaymentHold> findAllForExcel(PaymentHoldExcelSearchCommand command) {
        return queryFactory
            .selectFrom(paymentHold)
            .where(
                sellerEq(command.sellerId()),
                paymentHoldIdEq(command.paymentHoldId()),
                settlementNumberEq(command.settlementId()),
                statusEq(command.status()),
                dateGoe(command.startDate(), command.hasIdSearch(), command.dateType()),
                dateLoe(command.endDate(), command.hasIdSearch(), command.dateType())
            )
            .orderBy(paymentHold.id.desc())
            .fetch();
    }

    private BooleanExpression sellerEq(Long sellerId) {
        return paymentHold.seller.id.eq(sellerId);
    }

    private BooleanExpression paymentHoldIdEq(Long paymentHoldIdValue) {
        return paymentHoldIdValue != null ? paymentHold.id.eq(paymentHoldIdValue) : null;
    }

    private BooleanExpression settlementNumberEq(String settlementId) {
        return settlementId != null ? paymentHold.settlementNumber.eq(settlementId) : null;
    }

    private BooleanExpression statusEq(PaymentHoldStatus status) {
        return status != null ? paymentHold.status.eq(status) : null;
    }

    private BooleanExpression dateGoe(LocalDate startDate, boolean ignoreDateRange, PaymentHoldDateType dateType) {
        if (ignoreDateRange || startDate == null) {
            return null;
        }
        if (dateType == PaymentHoldDateType.COMPLETED_DATE) {
            return paymentHold.completedDate.goe(startDate);
        }
        return paymentHold.baseDate.goe(startDate);
    }

    private BooleanExpression dateLoe(LocalDate endDate, boolean ignoreDateRange, PaymentHoldDateType dateType) {
        if (ignoreDateRange || endDate == null) {
            return null;
        }
        if (dateType == PaymentHoldDateType.COMPLETED_DATE) {
            return paymentHold.completedDate.loe(endDate);
        }
        return paymentHold.baseDate.loe(endDate);
    }
}
