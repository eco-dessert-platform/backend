package com.bbangle.bbangle.board.seller.order.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class OrderRequest {

    public record OrderSearchRequest(
        @NotNull
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
        @Schema(description = "시작일", example = "2025-08-11")
        String startDate,
        @NotNull
        @Schema(description = "종료일", example = "2025-08-12")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
        String endDate,
        @Schema(description = "배송상태", allowableValues = {"ALL", "PAYMENT_COMPLETED",
            "PREPARING_ORDER", "SHIPPED", "DELIVERED"}, defaultValue = "ALL")
        String deliveryStatus,
        @Schema(description = "키워드 타입", allowableValues = {"ORDER_NUMBER", "BUYER", "ITEM_NAME",
            "TRACKING_NUMBER"}, defaultValue = "ORDER_NUMBER")
        String fieldType,
        @Schema(description = "키워드", example = "건강빵")
        String keyword,
        @Schema(description = "페이지 번호", defaultValue = "0", example = "0")
        Integer page,
        @Schema(description = "페이지 크기", defaultValue = "100", example = "100")
        Integer size
    ) {

    }
}
