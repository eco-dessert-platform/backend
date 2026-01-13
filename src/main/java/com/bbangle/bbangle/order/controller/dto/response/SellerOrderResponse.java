package com.bbangle.bbangle.order.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
}
