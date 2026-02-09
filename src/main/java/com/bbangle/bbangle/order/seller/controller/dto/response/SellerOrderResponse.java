package com.bbangle.bbangle.order.seller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

public class SellerOrderResponse {

    @Builder
    @Schema(description = "판매자 주문상품 발주 확인 응답 DTO")
    public record OrderConfirmResponse(

        @Schema(description = "발주 확인 결과 컨텐츠")
        Content content

    ) {

        public static OrderConfirmResponse of(Content content) {
            return OrderConfirmResponse.builder()
                .content(content)
                .build();
        }
    }

    @Builder
    @Schema(description = "발주 확인 응답 컨텐츠")
    public record Content(

        @Schema(description = "주문 ID")
        Long orderId,

        @Schema(description = "발주 확인 요약 정보")
        Summary summary,

        @Schema(description = "발주 확인 완료된 주문상품 ID 목록")
        List<Long> confirmedOrderItemIds,

        @Schema(description = "발주 확인 실패한 주문상품 ID 목록")
        List<Long> failedOrderItemIds

    ) {
        public static Content of(
            Long orderId,
            Summary summary,
            List<Long> confirmedOrderItemIds,
            List<Long> failedOrderItemIds
        ) {
            return Content.builder()
                .orderId(orderId)
                .summary(summary)
                .confirmedOrderItemIds(confirmedOrderItemIds)
                .failedOrderItemIds(failedOrderItemIds)
                .build();
        }
    }

    @Builder
    @Schema(description = "발주 확인 요약 정보")
    public record Summary(

        @Schema(description = "요청한 주문상품 수")
        int requestedCount,

        @Schema(description = "발주 확인 성공 수")
        int successCount,

        @Schema(description = "발주 확인 실패 수")
        int failCount

    ) {
        public static Summary of(
            int requestedCount,
            int successCount,
            int failCount
        ) {
            return Summary.builder()
                .requestedCount(requestedCount)
                .successCount(successCount)
                .failCount(failCount)
                .build();
        }
    }
}
