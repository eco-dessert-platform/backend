package com.bbangle.bbangle.order.seller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
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

        @Schema(description = "처리 요약 정보")
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
    @Schema(description = "처리 요약 정보")
    public record Summary(

        @Schema(description = "요청한 주문상품 수")
        int requestedCount,

        @Schema(description = "성공 수")
        int successCount,

        @Schema(description = "실패 수")
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

    @Builder
    @Schema(description = "판매자 운송장 정보 등록 응답 DTO")
    public record ShipmentRegisterResponse(

        @Schema(description = "운송장 등록 결과 컨텐츠")
        ShipmentContent content

    ) {
        public static ShipmentRegisterResponse of(ShipmentContent content) {
            return ShipmentRegisterResponse.builder()
                .content(content)
                .build();
        }
    }

    @Builder
    @Schema(description = "운송장 등록 응답 컨텐츠")
    public record ShipmentContent(

        @Schema(description = "주문 ID")
        Long orderId,

        @Schema(description = "처리 요약 정보")
        Summary summary,

        @Schema(description = "운송장 등록 성공한 주문상품 ID 목록")
        List<Long> successOrderItemIds,

        @Schema(description = "운송장 등록 실패한 주문상품 ID 목록")
        List<Long> failedOrderItemIds,

        @Schema(description = "택배사명")
        String courierName,

        @Schema(description = "운송장번호")
        String trackingNumber,

        @Schema(description = "출고 일시")
        LocalDateTime shippedAt

    ) {
        public static ShipmentContent of(
            Long orderId,
            Summary summary,
            List<Long> successOrderItemIds,
            List<Long> failedOrderItemIds,
            String courierName,
            String trackingNumber,
            LocalDateTime shippedAt
        ) {
            return ShipmentContent.builder()
                .orderId(orderId)
                .summary(summary)
                .successOrderItemIds(successOrderItemIds)
                .failedOrderItemIds(failedOrderItemIds)
                .courierName(courierName)
                .trackingNumber(trackingNumber)
                .shippedAt(shippedAt)
                .build();
        }
    }
}
