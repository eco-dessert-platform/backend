package com.bbangle.bbangle.paymenthold.seller.excel.service.model;

import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldDateType;
import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldStatus;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record PaymentHoldExcelSearchCommand(
    Long sellerId,
    LocalDate startDate,
    LocalDate endDate,
    PaymentHoldDateType dateType,
    PaymentHoldStatus status,
    Long paymentHoldId,
    String settlementId
) {

    public boolean hasIdSearch() {
        return paymentHoldId != null || settlementId != null;
    }
}
