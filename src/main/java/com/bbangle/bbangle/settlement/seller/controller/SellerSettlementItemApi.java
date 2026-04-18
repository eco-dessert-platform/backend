package com.bbangle.bbangle.settlement.seller.controller;

import com.bbangle.bbangle.common.dto.SingleResult;
import com.bbangle.bbangle.settlement.seller.controller.dto.request.SettlementItemFilter;
import com.bbangle.bbangle.settlement.seller.controller.dto.response.SettlementItemResponse.SettlementItemPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

/**
 * 판매자 건별 정산 내역 API Swagger 인터페이스.
 */
@Tag(name = "Seller Settlement", description = "(판매자) 정산 API")
public interface SellerSettlementItemApi {

    @Operation(summary = "(판매자) 건별 정산내역 페이징 조회",
        description = "baseDate 기준 날짜 범위로 건별 정산 내역 목록과 요약 정보(총 건수, 총 정산 예정 금액)를 반환합니다.")
    SingleResult<SettlementItemPageResponse> getSettlementItems(
        @ParameterObject Pageable pageable,
        @ParameterObject SettlementItemFilter filter,
        @Parameter(hidden = true) Long sellerId
    );

}
