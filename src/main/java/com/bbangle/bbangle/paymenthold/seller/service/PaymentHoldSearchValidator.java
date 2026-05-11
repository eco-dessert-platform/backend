package com.bbangle.bbangle.paymenthold.seller.service;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.paymenthold.seller.excel.service.model.PaymentHoldExcelSearchCommand;
import com.bbangle.bbangle.paymenthold.seller.service.model.PaymentHoldSearchCommand;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class PaymentHoldSearchValidator {

    /**
     * 목록 조회 조건을 검증한다.
     * paymentHoldId 또는 settlementId 검색이면 기간 제한 없이 조회할 수 있다.
     */
    public void validateLookup(PaymentHoldSearchCommand command) {
        validateDateOrder(command.startDate(), command.endDate());
        if (command.hasIdSearch()) {
            return;
        }
        validateMaxOneMonthRange(command.startDate(), command.endDate());
    }

    /**
     * 엑셀 다운로드 조건을 검증한다.
     * 일반 기간 조회는 날짜가 필수이며, ID 검색이면 날짜 없이도 허용한다.
     */
    public void validateExcel(PaymentHoldExcelSearchCommand command) {
        if (command.hasIdSearch()) {
            validateDateOrder(command.startDate(), command.endDate());
            return;
        }
        if (command.startDate() == null || command.endDate() == null) {
            throw new BbangleException(BbangleErrorCode.PAYMENT_HOLD_DATE_REQUIRED);
        }
        validateDateOrder(command.startDate(), command.endDate());
        validateMaxOneMonthRange(command.startDate(), command.endDate());
    }

    private void validateDateOrder(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BbangleException(BbangleErrorCode.INVALID_PAYMENT_HOLD_DATE_RANGE);
        }
    }

    private void validateMaxOneMonthRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isAfter(startDate.plusMonths(1))) {
            throw new BbangleException(BbangleErrorCode.PAYMENT_HOLD_DATE_RANGE_EXCEEDED);
        }
    }
}
