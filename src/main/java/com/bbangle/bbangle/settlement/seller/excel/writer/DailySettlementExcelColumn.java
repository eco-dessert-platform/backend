package com.bbangle.bbangle.settlement.seller.excel.writer;

import com.bbangle.bbangle.settlement.seller.controller.dto.response.DailySettlementResponse.DailySettlementSummary;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * 일별 정산 내역 엑셀의 컬럼 정의.
 * 순서, 한글 헤더명, 값 추출 방법을 한 곳에서 관리하여
 * 컬럼 추가/삭제/순서 변경이 이 enum 한 곳의 수정으로 완결된다.
 */
public enum DailySettlementExcelColumn {

    SETTLEMENT_NUMBER("정산ID",
        row -> row.settlementNumber()),

    SCHEDULED_DATE("정산예정일",
        row -> formatDate(row.scheduledDate())),

    COMPLETED_DATE("정산완료일",
        row -> formatDate(row.completedDate())),

    TOTAL_SETTLEMENT_AMOUNT("정산금액(a+b+c+d)",
        row -> row.totalSettlementAmount()),

    AMOUNT("결제금액(a)",
        row -> row.amount()),

    FEE("수수료(b)",
        row -> row.fee()),

    DEDUCTIBLE_REFUND("공제/환급(c)",
        row -> row.deductibleRefund()),

    DELIVERY_FEE_CHANGE("공제/환급 상세 - 배송비 금액 변동",
        row -> row.deductibleRefundDetail() != null ? row.deductibleRefundDetail().deliveryFeeChange() : null),

    BALANCE_OFFSET("공제/환급 상세 - 충전금 상계",
        row -> row.deductibleRefundDetail() != null ? row.deductibleRefundDetail().balanceOffset() : null),

    WITH_HOLDING_PAYMENT("지급보류(d)",
        row -> row.withHoldingPayment()),

    SETTLEMENT_METHOD("정산방식",
        row -> row.settlementMethod());

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String headerName;
    private final Function<DailySettlementSummary, Object> valueExtractor;

    DailySettlementExcelColumn(String headerName, Function<DailySettlementSummary, Object> valueExtractor) {
        this.headerName = headerName;
        this.valueExtractor = valueExtractor;
    }

    public String getHeaderName() {
        return headerName;
    }

    /**
     * 정산 행에서 이 컬럼에 해당하는 값을 추출한다.
     * BigDecimal은 숫자 셀로, 나머지는 문자열로 셀에 작성할 수 있도록 Object로 반환한다.
     */
    public Object extractValue(DailySettlementSummary row) {
        return valueExtractor.apply(row);
    }

    private static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    /**
     * 컬럼 값이 금액(BigDecimal)인지 여부.
     * 숫자 셀 스타일 적용 여부를 결정할 때 사용한다.
     */
    public static boolean isMoneyValue(Object value) {
        return value instanceof BigDecimal;
    }

}
