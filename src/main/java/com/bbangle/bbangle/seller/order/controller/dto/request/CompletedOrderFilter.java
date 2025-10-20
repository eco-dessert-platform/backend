package com.bbangle.bbangle.seller.order.controller.dto.request;

import com.bbangle.bbangle.seller.order.controller.dto.CompletedOrderSearchType;
import com.bbangle.bbangle.seller.order.controller.dto.CompletedOrderStatus;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDate;

public record CompletedOrderFilter(
    @Parameter(description = "조회 시작일", example = "2024-01-01") LocalDate startDate,
    @Parameter(description = "조회 종료일", example = "2024-01-31") LocalDate endDate,
    @Parameter(description = "완료주문 상태") CompletedOrderStatus status,
    @Parameter(description = "검색 상세 조건") CompletedOrderSearchType searchType,
    @Parameter(description = "검색어", example = "저당 베이글") String searchValue
) {

}
