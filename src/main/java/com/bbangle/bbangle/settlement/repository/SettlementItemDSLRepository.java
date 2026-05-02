package com.bbangle.bbangle.settlement.repository;

import com.bbangle.bbangle.settlement.repository.dao.SettlementItemDetailDao;
import com.bbangle.bbangle.settlement.repository.dao.SettlementItemSummaryDao;
import com.bbangle.bbangle.settlement.seller.excel.service.model.SettlementItemExcelSearchCommand;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.SettlementItemSearchCommand;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 건별 정산 내역 QueryDSL 커스텀 레포지토리 인터페이스.
 * 페이지네이션 조회, 요약 집계, 엑셀 전체 조회를 제공한다.
 */
public interface SettlementItemDSLRepository {

    /**
     * 건별 정산 내역 페이지네이션 조회.
     * sellerId와 날짜 범위(baseDate 기준)로 필터링하며 id 내림차순으로 반환한다.
     */
    Page<SettlementItemDetailDao> searchSettlementItems(SettlementItemSearchCommand command);

    /**
     * 건별 정산 요약 집계 조회.
     * 총 건수와 총 정산 예정 금액을 집계하여 반환한다.
     */
    SettlementItemSummaryDao fetchSummary(SettlementItemSearchCommand command);

    /**
     * 엑셀 다운로드용 건별 정산 전체 조회.
     * 페이지네이션 없이 날짜 범위 내 전체 데이터를 반환한다.
     */
    List<SettlementItemDetailDao> findAllForExcel(SettlementItemExcelSearchCommand command);

}
