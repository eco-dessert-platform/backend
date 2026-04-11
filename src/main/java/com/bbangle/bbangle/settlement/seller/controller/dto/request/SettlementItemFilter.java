package com.bbangle.bbangle.settlement.seller.controller.dto.request;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.settlement.seller.excel.service.model.SettlementItemExcelSearchCommand;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.SettlementItemSearchCommand;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;

/**
 * 건별 정산 내역 조회 요청 필터.
 * 날짜 범위 필터링과 엑셀 다운로드용 추가 검증을 제공한다.
 */
public record SettlementItemFilter(
    @Parameter(description = "조회 시작일 (baseDate 기준)", example = "2025-03-01")
    LocalDate startDate,

    @Parameter(description = "조회 종료일 (baseDate 기준)", example = "2025-03-31")
    LocalDate endDate
) {

    /**
     * Query Parameter로 수신한 필터 조건을 서비스 계층 커맨드로 변환한다.
     * sellerId: JWT 인증 정보에서 추출, pageable: Spring이 쿼리 파라미터에서 변환
     */
    public SettlementItemSearchCommand toCommand(Long sellerId, Pageable pageable) {
        validateDateRange();

        return SettlementItemSearchCommand.builder()
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

    /**
     * 엑셀 다운로드용 검증 후 조회 커맨드로 변환한다.
     * 내부적으로 validateForExcel()을 호출하여 날짜 필수 및 1개월 이내 범위를 강제한다.
     */
    public SettlementItemExcelSearchCommand toExcelCommand(Long sellerId) {
        validateForExcel();

        return SettlementItemExcelSearchCommand.builder()
            .sellerId(sellerId)
            .startDate(startDate)
            .endDate(endDate)
            .build();
    }

    private void validateDateRange() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BbangleException(BbangleErrorCode.INVALID_SETTLEMENT_DATE_RANGE);
        }
    }

}
