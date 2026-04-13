package com.bbangle.bbangle.statistics.domain.model;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import java.time.LocalDate;
import java.util.Objects;

public record DateRange(LocalDate startDate, LocalDate endDate) {

    public DateRange {
        Objects.requireNonNull(startDate, "시작일은 필수입니다.");
        Objects.requireNonNull(endDate, "마지막날은 필수입니다.");

        if (startDate.isAfter(endDate)) {
            throw new BbangleException(BbangleErrorCode.INVALID_DATE_RANGE);
        }
    }
}
