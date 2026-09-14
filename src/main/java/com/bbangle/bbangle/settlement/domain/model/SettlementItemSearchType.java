package com.bbangle.bbangle.settlement.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "건별 정산 내역 검색 구분")
public enum SettlementItemSearchType {

    @Schema(description = "주문번호 (Order.orderNumber)")
    ORDER_NUMBER,

    @Schema(description = "상품주문번호 (OrderItem.id)")
    ORDER_ITEM_ID

}
