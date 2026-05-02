package com.bbangle.bbangle.settlement.seller.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.settlement.seller.controller.dto.request.DailySettlementFilter;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.DailySettlementResponse.DailySettlementPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Seller Settlement", description = "(판매자) 정산 API")
public interface SellerSettlementApi {

    @Operation(summary = "(판매자) 일별 정산내역 페이징 조회")
    SingleResult<DailySettlementPageResponse> getDailySettlements(
        @ParameterObject Pageable pageable,
        @ParameterObject DailySettlementFilter filter,
        @Parameter(hidden = true) Long sellerId
    );

}
