package com.bbangle.bbangle.settlement.seller.service;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.settlement.domain.DailySettlement;
import com.bbangle.bbangle.settlement.repository.DailySettlementRepository;
import com.bbangle.bbangle.settlement.repository.dao.DailySettlementSummaryDao;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.DailySettlementResponse.DailySettlementPageResponse;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.DailySettlementResponse.DailySettlementSummary;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.DailySettlementResponse.SettlementSummaryInfo;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.DailySettlementSearchCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerSettlementService {

    private final DailySettlementRepository dailySettlementRepository;

    public DailySettlementPageResponse getDailySettlements(
        DailySettlementSearchCommand command
    ) {
        // 페이징 목록 조회
        Page<DailySettlement> entityPage =
            dailySettlementRepository.searchDailySettlements(command);

        // 엔티티 → DTO 변환
        List<DailySettlementSummary> summaries = entityPage.getContent().stream()
            .map(DailySettlementSummary::from)
            .toList();

        BbanglePageResponse<DailySettlementSummary> dtoPage = new BbanglePageResponse<>(
            summaries,
            entityPage.getNumber(),
            entityPage.getSize(),
            entityPage.getTotalPages(),
            entityPage.getTotalElements()
        );

        // 요약 정보 조회 (DB 집계) → DAO → Response DTO 변환
        DailySettlementSummaryDao summaryDao =
            dailySettlementRepository.fetchSummary(command);

        SettlementSummaryInfo summary = new SettlementSummaryInfo(
            summaryDao.scheduledDateMin(),
            summaryDao.scheduledDateMax(),
            summaryDao.totalSettlementAmount()
        );

        return new DailySettlementPageResponse(dtoPage, summary);
    }

}
