package com.bbangle.bbangle.settlement.seller.excel.service.model;

import java.time.LocalDate;
import lombok.Builder;

/**
 * 건별 정산 내역 엑셀 다운로드용 조회 커맨드.
 * 페이지네이션 없이 전체 데이터를 조회하므로 Pageable 필드를 포함하지 않는다.
 */
@Builder
public record SettlementItemExcelSearchCommand(
    Long sellerId,
    LocalDate startDate,
    LocalDate endDate
) {

}
