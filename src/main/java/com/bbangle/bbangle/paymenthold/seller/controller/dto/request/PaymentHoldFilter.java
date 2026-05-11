package com.bbangle.bbangle.paymenthold.seller.controller.dto.request;

import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldDateType;
import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldSearchStatus;
import com.bbangle.bbangle.paymenthold.domain.model.PaymentHoldStatus;
import com.bbangle.bbangle.paymenthold.seller.excel.service.model.PaymentHoldExcelSearchCommand;
import com.bbangle.bbangle.paymenthold.seller.service.model.PaymentHoldSearchCommand;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;

public record PaymentHoldFilter(
    @Parameter(description = "조회 기준", example = "BASE_DATE")
    PaymentHoldDateType dateType,

    @Parameter(description = "조회 시작일", example = "2025-03-01")
    LocalDate startDate,

    @Parameter(description = "조회 종료일", example = "2025-03-31")
    LocalDate endDate,

    @Parameter(description = "조회 상태", example = "ALL")
    PaymentHoldSearchStatus status,

    @Parameter(description = "지급보류 ID", example = "1")
    Long paymentHoldId,

    @Parameter(description = "정산 ID", example = "250401A1F7")
    String settlementId
) {

    public PaymentHoldSearchCommand toCommand(Long sellerId, Pageable pageable) {
        return PaymentHoldSearchCommand.builder()
            .sellerId(sellerId)
            .startDate(startDate)
            .endDate(endDate)
            .dateType(normalizedDateType())
            .status(toDomainStatus())
            .paymentHoldId(paymentHoldId)
            .settlementId(normalize(settlementId))
            .pageable(pageable)
            .build();
    }

    public PaymentHoldExcelSearchCommand toExcelCommand(Long sellerId) {
        return PaymentHoldExcelSearchCommand.builder()
            .sellerId(sellerId)
            .startDate(startDate)
            .endDate(endDate)
            .dateType(normalizedDateType())
            .status(toDomainStatus())
            .paymentHoldId(paymentHoldId)
            .settlementId(normalize(settlementId))
            .build();
    }

    private PaymentHoldStatus toDomainStatus() {
        PaymentHoldSearchStatus normalizedStatus = status != null ? status : PaymentHoldSearchStatus.ALL;
        return switch (normalizedStatus) {
            case ALL -> null;
            case ON_HOLD -> PaymentHoldStatus.ON_HOLD;
            case RELEASED -> PaymentHoldStatus.RELEASED;
        };
    }

    private PaymentHoldDateType normalizedDateType() {
        return dateType != null ? dateType : PaymentHoldDateType.BASE_DATE;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
