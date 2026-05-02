package com.bbangle.bbangle.settlement.seller.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.settlement.seller.controller.dto.request.DailySettlementFilter;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.DailySettlementResponse.DailySettlementPageResponse;
import com.bbangle.bbangle.settlement.seller.service.SellerSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/settlements/daily")
public class SellerSettlementController implements SellerSettlementApi {

    private final ResponseService responseService;
    private final SellerSettlementService sellerSettlementService;

    /**
     * 일별 정산내역 페이징 조회
     * 조회기간(startDate~endDate) 필터와 페이지네이션을 적용하여
     * 정산 목록 + 요약 정보(정산예정일 범위, 총 정산 금액)를 반환합니다.
     */
    @Override
    @GetMapping
    public SingleResult<DailySettlementPageResponse> getDailySettlements(
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
        Pageable pageable,
        DailySettlementFilter filter,
        @AuthenticationPrincipal Long sellerId
    ) {
        DailySettlementPageResponse response =
            sellerSettlementService.getDailySettlements(filter.toCommand(sellerId, pageable));
        return responseService.getSingleResult(response);
    }

}
