package com.bbangle.bbangle.order.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 검색 조건")
public enum CompletedOrderSearchType {

    @Schema(description = "주문자")
    BUYER_NAME,

    @Schema(description = "주문번호")
    ORDER_NUMBER,

    @Schema(description = "상품명")
    PRODUCT_NAME,

    @Schema(description = "송장번호")
    TRACKING_NUMBER

}
