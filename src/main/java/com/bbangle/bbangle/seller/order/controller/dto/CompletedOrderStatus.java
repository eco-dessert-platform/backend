package com.bbangle.bbangle.seller.order.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Schema(description = "완료주문상태")
public enum CompletedOrderStatus {

    PURCHASED("구매확정"),
    CANCELED("주문취소"),
    RETURNED("반품완료"),
    EXCHANGED("교환완료");

    private final String description;

}
