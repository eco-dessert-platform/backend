package com.bbangle.bbangle.paymenthold.seller.service.model;

import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldDateType;
import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldStatus;
import java.time.LocalDate;
import lombok.Builder;
import org.springframework.data.domain.Pageable;

@Builder
public record PaymentHoldSearchCommand(
    Long sellerId,
    LocalDate startDate,
    LocalDate endDate,
    PaymentHoldDateType dateType,
    PaymentHoldStatus status,
    Long paymentHoldId,
    String settlementId,
    Pageable pageable
) {

    public boolean hasIdSearch() {
        return paymentHoldId != null || settlementId != null;
    }
}
