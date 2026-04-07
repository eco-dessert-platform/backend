package com.bbangle.bbangle.settlement.repository;

import com.bbangle.bbangle.settlement.domain.DailySettlement;
import com.bbangle.bbangle.settlement.repository.dao.DailySettlementSummaryDao;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.DailySettlementSearchCommand;
import org.springframework.data.domain.Page;

public interface DailySettlementDSLRepository {

    // 페이징 목록 조회
    Page<DailySettlement> searchDailySettlements(DailySettlementSearchCommand command);

    // 요약 정보 조회 (정산예정일 min/max, 총 정산금액)
    DailySettlementSummaryDao fetchSummary(DailySettlementSearchCommand command);

}
