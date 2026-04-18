package com.bbangle.bbangle.settlement.seller.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.common.service.ResponseService;
import com.bbangle.bbangle.config.security.SellerApiPath;
import com.bbangle.bbangle.settlement.seller.controller.dto.request.SettlementItemFilter;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.SettlementItemResponse.SettlementItemPageResponse;
import com.bbangle.bbangle.settlement.seller.service.SellerSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 판매자 건별 정산 내역 조회 컨트롤러.
 * GET /api/v1/seller/settlements/items
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(SellerApiPath.PREFIX + "/settlements/items")
public class SellerSettlementItemController implements SellerSettlementItemApi {

    private final ResponseService responseService;
    private final SellerSettlementService sellerSettlementService;

    /**
     * 건별 정산내역 페이징 조회.
     * 조회기간(startDate~endDate, baseDate 기준) 필터와 페이지네이션을 적용하여
     * 정산 목록 + 요약 정보(총 건수, 총 정산 예정 금액)를 반환한다.
     */
    @Override
    @GetMapping
    public SingleResult<SettlementItemPageResponse> getSettlementItems(
        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
        Pageable pageable,
        SettlementItemFilter filter,
        @AuthenticationPrincipal Long sellerId
    ) {
        return responseService.getSingleResult(
            sellerSettlementService.getSettlementItems(filter.toCommand(sellerId, pageable))
        );
    }

}
