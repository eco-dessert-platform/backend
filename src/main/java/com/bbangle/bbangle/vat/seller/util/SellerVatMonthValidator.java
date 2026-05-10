package com.bbangle.bbangle.vat.seller.util;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

public final class SellerVatMonthValidator {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final Pattern MONTH_PATTERN = Pattern.compile("^\\d{4}-\\d{2}$");
    private static final long MAX_MONTHS = 12;

    private SellerVatMonthValidator() {
    }

    /**
     * API 조회 조건의 yyyy-MM 문자열을 YearMonth로 변환한다.
     * Spring 기본 바인딩 대신 명시적으로 검증해 VAT 전용 에러 코드를 반환한다.
     */
    public static YearMonth parse(String value) {
        if (value == null || !MONTH_PATTERN.matcher(value).matches()) {
            throw new BbangleException(BbangleErrorCode.INVALID_MONTH_FORMAT);
        }

        try {
            return YearMonth.parse(value, MONTH_FORMATTER);
        } catch (RuntimeException ex) {
            throw new BbangleException(BbangleErrorCode.INVALID_MONTH_FORMAT, ex);
        }
    }

    /**
     * 조회 시작 월과 종료 월의 순서 및 최대 12개월 범위를 검증한다.
     */
    public static void validateRange(YearMonth startMonth, YearMonth endMonth) {
        if (startMonth.isAfter(endMonth)) {
            throw new BbangleException(BbangleErrorCode.INVALID_VAT_DATE_RANGE);
        }

        long inclusiveMonths = ChronoUnit.MONTHS.between(startMonth, endMonth) + 1;
        if (inclusiveMonths > MAX_MONTHS) {
            throw new BbangleException(BbangleErrorCode.EXCEEDED_MAX_VAT_DATE_RANGE);
        }
    }
}
