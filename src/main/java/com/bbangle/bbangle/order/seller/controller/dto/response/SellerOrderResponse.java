package com.bbangle.bbangle.order.seller.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public class SellerOrderResponse {

    @Builder
    @Schema(description = "판매자 주문상품 발주 확인 응답 DTO")
    public record OrderConfirmResponse(

        @Schema(description = "주문 ID")
        Long orderId,

        @Schema(description = "발주 확인 완료된 주문상품 ID 목록")
        List<Long> confirmedOrderItemIds

    ) {

        public static OrderConfirmResponse of(
            Long orderId,
            List<Long> confirmedOrderItemIds
        ) {
            return OrderConfirmResponse.builder()
                .orderId(orderId)
                .confirmedOrderItemIds(confirmedOrderItemIds)
                .build();
        }
    }

    @Builder
    @Schema(description = "판매자 운송장 정보 등록 응답 DTO")
    public record ShipmentRegisterResponse(

        @Schema(description = "주문 ID")
        Long orderId,

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
        public static ShipmentRegisterResponse of(
            Long orderId,
            List<Long> successOrderItemIds,
            List<Long> failedOrderItemIds,
            String courierName,
            String trackingNumber,
            LocalDateTime shippedAt
        ) {
            return ShipmentRegisterResponse.builder()
                .orderId(orderId)
                .successOrderItemIds(successOrderItemIds)
                .failedOrderItemIds(failedOrderItemIds)
                .courierName(courierName)
                .trackingNumber(trackingNumber)
                .shippedAt(shippedAt)
                .build();
        }
    }
}
