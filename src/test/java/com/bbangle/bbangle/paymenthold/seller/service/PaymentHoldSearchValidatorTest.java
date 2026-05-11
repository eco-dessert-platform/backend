package com.bbangle.bbangle.paymenthold.seller.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldDateType;
import com.bbangle.bbangle.paymenthold.seller.excel.service.model.PaymentHoldExcelSearchCommand;
import com.bbangle.bbangle.paymenthold.seller.service.model.PaymentHoldSearchCommand;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

@DisplayName("[Service] PaymentHoldSearchValidator")
class PaymentHoldSearchValidatorTest {

    private final PaymentHoldSearchValidator validator = new PaymentHoldSearchValidator();

    @Test
    @DisplayName("일반 조회 기간이 1개월을 초과하면 PAYMENT_HOLD_DATE_RANGE_EXCEEDED 예외가 발생한다")
    void validateLookup_overOneMonth_throwsPaymentHoldRangeExceeded() {
        PaymentHoldSearchCommand command = PaymentHoldSearchCommand.builder()
            .sellerId(1L)
            .dateType(PaymentHoldDateType.BASE_DATE)
            .startDate(LocalDate.of(2026, 1, 1))
            .endDate(LocalDate.of(2026, 2, 2))
            .pageable(PageRequest.of(0, 10))
            .build();

        assertThatThrownBy(() -> validator.validateLookup(command))
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.PAYMENT_HOLD_DATE_RANGE_EXCEEDED));
    }

    @Test
    @DisplayName("ID 검색 조회는 기간이 길어도 허용한다")
    void validateLookup_withIdSearch_skipsRangeValidation() {
        PaymentHoldSearchCommand command = PaymentHoldSearchCommand.builder()
            .sellerId(1L)
            .dateType(PaymentHoldDateType.BASE_DATE)
            .startDate(LocalDate.of(2026, 1, 1))
            .endDate(LocalDate.of(2026, 3, 1))
            .settlementId("260401A1F7")
            .pageable(PageRequest.of(0, 10))
            .build();

        assertThatCode(() -> validator.validateLookup(command)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("엑셀 다운로드에서 날짜가 없으면 PAYMENT_HOLD_DATE_REQUIRED 예외가 발생한다")
    void validateExcel_withoutDates_throwsPaymentHoldDateRequired() {
        PaymentHoldExcelSearchCommand command = PaymentHoldExcelSearchCommand.builder()
            .sellerId(1L)
            .dateType(PaymentHoldDateType.BASE_DATE)
            .build();

        assertThatThrownBy(() -> validator.validateExcel(command))
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.PAYMENT_HOLD_DATE_REQUIRED));
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦으면 INVALID_PAYMENT_HOLD_DATE_RANGE 예외가 발생한다")
    void validateExcel_invalidDateOrder_throwsInvalidPaymentHoldDateRange() {
        PaymentHoldExcelSearchCommand command = PaymentHoldExcelSearchCommand.builder()
            .sellerId(1L)
            .dateType(PaymentHoldDateType.COMPLETED_DATE)
            .startDate(LocalDate.of(2026, 4, 5))
            .endDate(LocalDate.of(2026, 4, 1))
            .build();

        assertThatThrownBy(() -> validator.validateExcel(command))
            .isInstanceOf(BbangleException.class)
            .satisfies(e -> assertThat(((BbangleException) e).getBbangleErrorCode())
                .isEqualTo(BbangleErrorCode.INVALID_PAYMENT_HOLD_DATE_RANGE));
    }
}
