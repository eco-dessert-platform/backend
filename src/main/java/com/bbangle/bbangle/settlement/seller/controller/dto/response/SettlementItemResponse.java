package com.bbangle.bbangle.settlement.seller.controller.dto.response;

import com.bbangle.bbangle.common.page.BbanglePageResponse;
import com.bbangle.bbangle.settlement.repository.dao.SettlementItemDetailDao;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 건별 정산 내역 API 응답 DTO 묶음 클래스.
 */
public class SettlementItemResponse {

    @Schema(description = "건별 정산내역 조회 응답 (페이지 + 요약)")
    public record SettlementItemPageResponse(
        @Schema(description = "건별 정산 목록 페이지")
        BbanglePageResponse<SettlementItemSummary> settlements,

        @Schema(description = "정산 요약 정보")
        SettlementItemSummaryInfo summary
    ) {

    }

    @Schema(description = "건별 정산 요약 정보")
    public record SettlementItemSummaryInfo(
        @Schema(description = "총 정산 건수", example = "42")
        long totalCount,

        @Schema(description = "총 정산 예정 금액", example = "5000000")
        BigDecimal totalScheduledAmount
    ) {

    }

    @Schema(description = "건별 정산 내역")
    public record SettlementItemSummary(
        @Schema(description = "주문번호", example = "ORD20250301001")
        String orderNumber,

        @Schema(description = "상품주문번호(OrderItem ID)", example = "1001")
        Long orderItemId,

        @Schema(description = "판매자 ID", example = "9001")
        Long sellerId,

        @Schema(description = "구매자명", example = "홍길동")
        String buyerName,

        @Schema(description = "상품명", example = "글루텐프리 식빵")
        String productTitle,

        @Schema(description = "정산 예정 금액", example = "25000")
        BigDecimal scheduledAmount,

        @Schema(description = "수량", example = "2")
        Integer quantity,

        @Schema(description = "정산 시작일")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate settlementStartDate,

        @Schema(description = "정산 종료일")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate settlementEndDate,

        @Schema(description = "정산 예정일")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate scheduledDate,

        @Schema(description = "정산 상태", example = "정산대기")
        String status
    ) {

        /**
         * SettlementItemDetailDao를 SettlementItemSummary 응답 DTO로 변환한다.
         * settlementStartDate와 settlementEndDate 모두 baseDate 값을 사용한다.
         */
        public static SettlementItemSummary from(SettlementItemDetailDao dao) {
            return new SettlementItemSummary(
                dao.orderNumber(),
                dao.orderItemId(),
                dao.sellerId(),
                dao.buyerName(),
                dao.productTitle(),
                dao.scheduledAmount(),
                dao.quantity(),
                dao.baseDate(),   // 정산 시작일: baseDate
                dao.baseDate(),   // 정산 종료일: baseDate
                dao.scheduledDate(),
                dao.status() != null ? dao.status().getDescription() : null
            );
        }
    }

}
