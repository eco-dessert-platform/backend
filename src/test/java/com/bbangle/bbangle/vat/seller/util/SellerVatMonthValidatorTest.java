package com.bbangle.bbangle.vat.seller.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[Validator] SellerVatMonthValidator")
class SellerVatMonthValidatorTest {

    @Test
    @DisplayName("yyyy-MM 형식의 월 문자열을 YearMonth로 변환한다")
    void parse_returnsYearMonth() {
        assertThat(SellerVatMonthValidator.parse("2025-04"))
            .isEqualTo(YearMonth.of(2025, 4));
    }

    @Test
    @DisplayName("월 형식이 올바르지 않으면 INVALID_MONTH_FORMAT 예외를 던진다")
    void parse_throwsExceptionWhenMonthFormatInvalid() {
        assertThatThrownBy(() -> SellerVatMonthValidator.parse("2025-4"))
            .isInstanceOf(BbangleException.class)
            .extracting("bbangleErrorCode")
            .isEqualTo(BbangleErrorCode.INVALID_MONTH_FORMAT);
    }

    @Test
    @DisplayName("시작 월이 종료 월보다 뒤면 INVALID_VAT_DATE_RANGE 예외를 던진다")
    void validateRange_throwsExceptionWhenStartMonthAfterEndMonth() {
        assertThatThrownBy(() -> SellerVatMonthValidator.validateRange(
                YearMonth.of(2025, 5),
                YearMonth.of(2025, 4)
            ))
            .isInstanceOf(BbangleException.class)
            .extracting("bbangleErrorCode")
            .isEqualTo(BbangleErrorCode.INVALID_VAT_DATE_RANGE);
    }

    @Test
    @DisplayName("조회 기간이 12개월을 초과하면 EXCEEDED_MAX_VAT_DATE_RANGE 예외를 던진다")
    void validateRange_throwsExceptionWhenRangeExceedsTwelveMonths() {
        assertThatThrownBy(() -> SellerVatMonthValidator.validateRange(
                YearMonth.of(2024, 1),
                YearMonth.of(2025, 1)
            ))
            .isInstanceOf(BbangleException.class)
            .extracting("bbangleErrorCode")
            .isEqualTo(BbangleErrorCode.EXCEEDED_MAX_VAT_DATE_RANGE);
    }
}
