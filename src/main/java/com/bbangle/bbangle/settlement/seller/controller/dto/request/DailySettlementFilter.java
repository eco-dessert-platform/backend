package com.bbangle.bbangle.settlement.seller.controller.dto.request;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.DailySettlementSearchCommand;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;

public record DailySettlementFilter(
    @Parameter(description = "조회 시작일", example = "2025-03-01")
    LocalDate startDate,

    @Parameter(description = "조회 종료일", example = "2025-03-07")
    LocalDate endDate
) {

    /**
     * Query Parameter로 수신한 필터 조건을 서비스 계층 커맨드로 변환합니다.
     * sellerId: JWT 인증 정보에서 추출, pageable: Spring이 쿼리 파라미터에서 변환
     */
    public DailySettlementSearchCommand toCommand(Long sellerId, Pageable pageable) {
        validateDateRange();

        return DailySettlementSearchCommand.builder()
            .sellerId(sellerId)
            .startDate(startDate)
            .endDate(endDate)
            .pageable(pageable)
            .build();
    }

    private void validateDateRange() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BbangleException(BbangleErrorCode.INVALID_SETTLEMENT_DATE_RANGE);
        }
    }

}
