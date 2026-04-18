package com.bbangle.bbangle.settlement.seller.excel.writer;

import com.bbangle.bbangle.settlement.seller.controller.dto.response.SettlementItemResponse.SettlementItemSummary;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

/**
 * 건별 정산 내역 엑셀의 컬럼 정의.
 * 순서, 한글 헤더명, 값 추출 방법을 한 곳에서 관리하여
 * 컬럼 추가/삭제/순서 변경이 이 enum 한 곳의 수정으로 완결된다.
 */
public enum SettlementItemExcelColumn {

    ORDER_NUMBER("주문번호",
        row -> row.orderNumber()),

    ORDER_ITEM_ID("상품주문번호",
        row -> row.orderItemId() != null ? row.orderItemId().toString() : null),

    SELLER_ID("판매ID",
        row -> row.sellerId() != null ? row.sellerId().toString() : null),

    BUYER_NAME("구매자",
        row -> row.buyerName()),

    PRODUCT_TITLE("상품명",
        row -> row.productTitle()),

    SCHEDULED_AMOUNT("정산대상금액",
        row -> row.scheduledAmount()),

    QUANTITY("단위",
        row -> row.quantity() != null ? row.quantity() : null),

    SETTLEMENT_START_DATE("정산시작일",
        row -> formatDate(row.settlementStartDate())),

    SETTLEMENT_END_DATE("정산종료일",
        row -> formatDate(row.settlementEndDate())),

    SCHEDULED_DATE("정산예정일",
        row -> formatDate(row.scheduledDate())),

    STATUS("정산여부",
        row -> row.status());

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String headerName;
    private final Function<SettlementItemSummary, Object> valueExtractor;

    SettlementItemExcelColumn(String headerName, Function<SettlementItemSummary, Object> valueExtractor) {
        this.headerName = headerName;
        this.valueExtractor = valueExtractor;
    }

    public String getHeaderName() {
        return headerName;
    }

    /**
     * 건별 정산 행에서 이 컬럼에 해당하는 값을 추출한다.
     * BigDecimal은 숫자 셀로, 나머지는 문자열로 셀에 작성할 수 있도록 Object로 반환한다.
     */
    public Object extractValue(SettlementItemSummary row) {
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
