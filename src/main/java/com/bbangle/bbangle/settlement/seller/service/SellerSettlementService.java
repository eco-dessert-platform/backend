package com.bbangle.bbangle.settlement.seller.service;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.settlement.domain.DailySettlement;
import com.bbangle.bbangle.settlement.repository.DailySettlementRepository;
import com.bbangle.bbangle.settlement.repository.SettlementItemRepository;
import com.bbangle.bbangle.settlement.repository.dao.DailySettlementSummaryDao;
import com.bbangle.bbangle.settlement.repository.dao.SettlementItemDetailDao;
import com.bbangle.bbangle.settlement.repository.dao.SettlementItemSummaryDao;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.DailySettlementResponse.DailySettlementPageResponse;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.DailySettlementResponse.DailySettlementSummary;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.DailySettlementResponse.SettlementSummaryInfo;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.SettlementItemResponse.SettlementItemPageResponse;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.SettlementItemResponse.SettlementItemSummary;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.SettlementItemResponse.SettlementItemSummaryInfo;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.DailySettlementSearchCommand;
import com.bbangle.bbangle.settlement.seller.service.model.SellerSettlementCommand.SettlementItemSearchCommand;
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
    private final SettlementItemRepository settlementItemRepository;

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

    /**
     * 건별 정산 내역 페이지네이션 조회.
     * sellerId와 날짜 범위(baseDate 기준)로 필터링된 정산 목록과 요약 정보를 반환한다.
     */
    public SettlementItemPageResponse getSettlementItems(SettlementItemSearchCommand command) {
        // 페이징 목록 조회
        Page<SettlementItemDetailDao> itemPage =
            settlementItemRepository.searchSettlementItems(command);

        // DAO → 응답 DTO 변환
        List<SettlementItemSummary> summaries = itemPage.getContent().stream()
            .map(SettlementItemSummary::from)
            .toList();

        BbanglePageResponse<SettlementItemSummary> dtoPage = new BbanglePageResponse<>(
            summaries,
            itemPage.getNumber(),
            itemPage.getSize(),
            itemPage.getTotalPages(),
            itemPage.getTotalElements()
        );

        // 총 건수는 이미 페이징 쿼리에서 계산된 값을 재활용하여 불필요한 카운트 쿼리를 방지한다.
        SettlementItemSummaryDao summaryDao =
            settlementItemRepository.fetchSummary(command);

        SettlementItemSummaryInfo summary = new SettlementItemSummaryInfo(
            itemPage.getTotalElements(),
            summaryDao.totalScheduledAmount()
        );

        return new SettlementItemPageResponse(dtoPage, summary);
    }

}
