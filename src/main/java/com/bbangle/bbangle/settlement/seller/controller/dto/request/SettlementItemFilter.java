package com.bbangle.bbangle.settlement.seller.controller.dto.request;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.settlement.domain.model.SettlementItemDateType;
import com.bbangle.bbangle.settlement.domain.model.SettlementItemSearchType;
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
    @Parameter(description = "조회 기준 날짜 타입 (미전달 시 기존 호환을 위해 BASE_DATE 적용)", example = "SCHEDULED_DATE")
    SettlementItemDateType dateType,

    @Parameter(description = "조회 시작일 (dateType 기준)", example = "2025-03-01")
    LocalDate startDate,

    @Parameter(description = "조회 종료일 (dateType 기준)", example = "2025-03-31")
    LocalDate endDate,

    @Parameter(description = "검색 구분 (searchType 없이 검색어만 전달되면 검색조건은 무시된다)", example = "ORDER_NUMBER")
    SettlementItemSearchType searchType,

    @Parameter(description = "검색어", example = "250401A1F7")
    String searchValue
) {

    /**
     * Query Parameter로 수신한 필터 조건을 서비스 계층 커맨드로 변환한다.
     * sellerId: JWT 인증 정보에서 추출, pageable: Spring이 쿼리 파라미터에서 변환
     */
    public SettlementItemSearchCommand toCommand(Long sellerId, Pageable pageable) {
        validateDateRange();
        validateMaxOneMonthRange(startDate, endDate);

        return SettlementItemSearchCommand.builder()
            .sellerId(sellerId)
            .dateType(normalizedDateType())
            .startDate(startDate)
            .endDate(endDate)
            .searchType(searchType)
            .searchValue(normalize(searchValue))
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
        validateMaxOneMonthRange(startDate, endDate);
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

    /**
     * 조회 기간 최대 1개월 검증 (e.g. 01-01 ~ 02-01 허용, 01-01 ~ 02-02 불가).
     * startDate 또는 endDate가 없으면 검증하지 않는다 (기존 API 호환을 위해 optional 유지).
     */
    private void validateMaxOneMonthRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isAfter(start.plusMonths(1))) {
            throw new BbangleException(BbangleErrorCode.SETTLEMENT_DATE_RANGE_EXCEEDED);
        }
    }

    /**
     * dateType 기본값 처리.
     * 미전달 시 기존 API 호환을 위해 BASE_DATE(정산기준일)를 기본값으로 사용한다.
     * (피그마 기획상 기본값은 SCHEDULED_DATE이나, 신규 화면은 dateType을 항상 명시적으로 전달할 것을 전제로
     *  기존 호출(파라미터 미전달)의 필터링 기준을 유지하기 위해 서버 기본값은 BASE_DATE로 둔다.)
     */
    private SettlementItemDateType normalizedDateType() {
        return dateType != null ? dateType : SettlementItemDateType.BASE_DATE;
    }

    private String normalize(String value) {
        return (value != null && !value.isBlank()) ? value : null;
    }

}
