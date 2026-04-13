package com.bbangle.bbangle.settlement.seller.excel.repository;

import com.bbangle.bbangle.settlement.domain.DailySettlement;
import com.bbangle.bbangle.settlement.seller.excel.service.model.DailySettlementExcelSearchCommand;
import java.util.List;

/**
 * 엑셀 다운로드 전용 QueryDSL 레포지토리.
 * 페이지네이션 없이 조건에 맞는 전체 정산 내역을 조회한다.
 */
public interface DailySettlementExcelDSLRepository {

    /**
     * 엑셀 다운로드용 일별 정산 내역 전체 조회.
     * sellerId, startDate, endDate 조건을 적용하며 id 내림차순으로 반환한다.
     */
    List<DailySettlement> findAllForExcel(DailySettlementExcelSearchCommand command);

}
