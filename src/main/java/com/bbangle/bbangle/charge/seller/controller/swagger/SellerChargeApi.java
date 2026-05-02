package com.bbangle.bbangle.charge.seller.controller.swagger;

import com.bbangle.bbangle.charge.seller.controller.dto.request.WithdrawalRequest;
import com.bbangle.bbangle.charge.seller.controller.dto.response.ChargeBalanceResponse;
import com.bbangle.bbangle.common.dto.CommonResult;
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

    @Operation(
        summary = "(판매자) 충전금 출금 신청",
        description = "판매자가 보유한 충전금을 지정 계좌로 출금 신청합니다. "
            + "신청 즉시 잔액에서 차감되며, X-Transaction-Id 헤더로 중복 요청을 방지합니다."
    )
    CommonResult requestWithdrawal(
        @Parameter(hidden = true)
        Long sellerId,

        @Parameter(
            description = "중복 요청 방지용 고유 거래 ID (UUID, 요청 시 생성)",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true
        )
        String transactionId,

        WithdrawalRequest request
    );
}
