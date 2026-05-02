package com.bbangle.bbangle.order.seller.controller.dto.request;

import com.bbangle.bbangle.order.domain.model.CompletedOrderSearchType;
import com.bbangle.bbangle.order.domain.model.CompletedOrderStatus;
import com.bbangle.bbangle.order.seller.service.model.SellerOrderCommand.CompletedOrderSearchCommand;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;

public record CompletedOrderFilter(
    @Parameter(description = "조회 시작일", example = "2024-01-01") LocalDate startDate,
    @Parameter(description = "조회 종료일", example = "2024-01-31") LocalDate endDate,
    @Parameter(description = "완료주문 상태") CompletedOrderStatus status,
    @Parameter(description = "검색 상세 조건") CompletedOrderSearchType searchType,
    @Parameter(description = "검색어", example = "저당 베이글") String searchValue
) {
    /**
     * Query Parameter로 수신한 필터 조건을 서비스 계층 커맨드로 변환합니다.
     * sellerId와 pageable을 Filter 자체에 포함하지 않는 이유:
     * - sellerId: JWT 인증 정보에서 추출되어 Controller가 @AuthenticationPrincipal로 주입합니다.
     * - pageable: Spring HandlerMethodArgumentResolver가 쿼리 파라미터(page/size/sort)를 변환하여 주입합니다.
     */
    public CompletedOrderSearchCommand toCommand(Long sellerId, Pageable pageable) {
        return CompletedOrderSearchCommand.builder()
            .sellerId(sellerId)
            .startDate(startDate)
            .endDate(endDate)
            .status(status)
            .searchType(searchType)
            .searchValue(searchValue)
            .pageable(pageable)
            .build();
    }
}

