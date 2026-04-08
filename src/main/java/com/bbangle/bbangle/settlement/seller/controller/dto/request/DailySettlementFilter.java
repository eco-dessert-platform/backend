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

    /**
     * 엑셀 다운로드용 검증.
     * 시작일·종료일이 모두 필수이며, 조회 기간은 최대 1개월을 초과할 수 없다.
     */
    public void validateForExcel() {
        if (startDate == null || endDate == null) {
            throw new BbangleException(BbangleErrorCode.SETTLEMENT_DATE_REQUIRED);
        }
        validateDateRange();
        // startDate 기준 1개월 초과 여부 검증 (e.g. 01-01 ~ 02-01 허용, 01-01 ~ 02-02 불가)
        if (endDate.isAfter(startDate.plusMonths(1))) {
            throw new BbangleException(BbangleErrorCode.SETTLEMENT_DATE_RANGE_EXCEEDED);
        }
    }

    private void validateDateRange() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BbangleException(BbangleErrorCode.INVALID_SETTLEMENT_DATE_RANGE);
        }
    }

}
