package com.bbangle.bbangle.order.controller.dto.request;

import com.bbangle.bbangle.order.controller.dto.CompletedOrderStatus;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDate;

public record CompletedOrderFilter(
    @Parameter(description = "조회 시작일", example = "2024-01-01") LocalDate startDate,
    @Parameter(description = "조회 종료일", example = "2024-01-31") LocalDate endDate,
    @Parameter(description = "완료주문 상태") CompletedOrderStatus status
) {
}
