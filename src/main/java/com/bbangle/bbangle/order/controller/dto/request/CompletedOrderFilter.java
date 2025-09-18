package com.bbangle.bbangle.order.controller.dto.request;

import com.bbangle.bbangle.order.controller.dto.CompletedOrderStatus;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDateTime;

public record CompletedOrderFilter(
    @Parameter(description = "조회 시작일", example = "2024-01-01T00:00:00") LocalDateTime startDateTime,
    @Parameter(description = "조회 종료일", example = "2024-01-31T23:59:59") LocalDateTime endDateTime,
    @Parameter(description = "완료주문 상태") CompletedOrderStatus status
) {
}
