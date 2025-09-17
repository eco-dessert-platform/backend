package com.bbangle.bbangle.order.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "완료주문상태")
public enum CompletedOrderStatus {

    PURCHASED("구매확정"),
    CANCELED("주문취소"),
    RETURNED("반품완료"),
    EXCHANGED("교환완료");

    @JsonValue
    private final String description;

    CompletedOrderStatus(String description) {
        this.description = description;
    }

}
