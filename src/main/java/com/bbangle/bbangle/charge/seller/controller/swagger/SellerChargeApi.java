package com.bbangle.bbangle.charge.seller.controller.swagger;

import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse;
import com.bbangle.bbangle.common.dto.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;

@Tag(name = "Seller Charge", description = "(판매자) 충전금 API")
public interface SellerChargeApi {

    @Operation(
        summary = "(판매자) 충전금 현황 조회",
        description = "판매자의 현재 충전금 잔액과 기간 필터로 조회한 충전금 거래내역 목록을 반환합니다. "
            + "기본 조회 기간: 최근 1주일 / 최대 조회 기간: 1년"
    )
    SingleResult<ChargeBalanceResponse> getChargeBalance(
        @Parameter(hidden = true)
        Long sellerId,

        @Parameter(
            description = "조회 시작일 (기본값: 오늘 - 7일)",
            example = "2025-03-01"
        )
        LocalDate startDate,

        @Parameter(
            description = "조회 종료일 (기본값: 오늘)",
            example = "2025-03-07"
        )
        LocalDate endDate,

        @ParameterObject Pageable pageable
    );
}
