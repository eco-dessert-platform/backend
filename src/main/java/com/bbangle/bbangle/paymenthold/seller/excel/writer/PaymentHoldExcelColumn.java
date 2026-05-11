package com.bbangle.bbangle.paymenthold.seller.excel.writer;

import com.bbangle.bbangle.paymenthold.seller.controller.dto.response.PaymentHoldResponse.PaymentHoldSummary;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public enum PaymentHoldExcelColumn {

    PAYMENT_HOLD_ID("지급보류 ID", PaymentHoldSummary::paymentHoldId),
    SETTLEMENT_ID("정산 ID", PaymentHoldSummary::settlementId),
    STATUS("정산 상태", PaymentHoldSummary::status),
    SETTLEMENT_BASE_DATE("정산 기준일", row -> formatDate(row.settlementBaseDate())),
    SETTLEMENT_COMPLETED_DATE("정산 완료일", row -> formatDate(row.settlementCompletedDate())),
    SETTLEMENT_AMOUNT("정산 금액", PaymentHoldSummary::settlementAmount);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String headerName;
    private final Function<PaymentHoldSummary, Object> valueExtractor;

    PaymentHoldExcelColumn(String headerName, Function<PaymentHoldSummary, Object> valueExtractor) {
        this.headerName = headerName;
        this.valueExtractor = valueExtractor;
    }

    public String getHeaderName() {
        return headerName;
    }

    public Object extractValue(PaymentHoldSummary row) {
        return valueExtractor.apply(row);
    }

    public static boolean isMoneyValue(Object value) {
        return value instanceof BigDecimal;
    }

    private static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
}
