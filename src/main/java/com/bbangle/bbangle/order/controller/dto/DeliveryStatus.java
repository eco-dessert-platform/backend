package com.bbangle.bbangle.order.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "배송 상태")
public enum DeliveryStatus {

    @Schema(description = "배송 준비중 (상품 포장 등)") READY,
    @Schema(description = "배송중 (택배사 집하 완료)") IN_TRANSIT,
    @Schema(description = "배송지 인근 도착") OUT_FOR_DELIVERY,
    @Schema(description = "배송 완료") DELIVERED,
    @Schema(description = "배송 실패 (고객 부재, 주소 오류 등)") FAILED,
    @Schema(description = "수거중") PICKUP,

}
