package com.bbangle.bbangle.seller.order.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 상태")
public enum OrderStatus {

    @Schema(description = "결제 완료") PAID,
    @Schema(description = "상품 발송") SHIPPED,
    @Schema(description = "발주 확인") CONFIRMED,
    @Schema(description = "배송 완료") DELIVERED,
    @Schema(description = "주문 취소") CANCELED,
    @Schema(description = "반품 처리중") RETURN_PENDING,
    @Schema(description = "교환 처리중") EXCHANGE_PENDING,
    @Schema(description = "반품 완료") RETURN_COMPLETED,
    @Schema(description = "교환 완료") EXCHANGE_COMPLETED,
    @Schema(description = "구매 확정") PURCHASE_CONFIRMED

}
