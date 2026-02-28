package com.bbangle.bbangle.board.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryType {

    FREE("무료"),
    CONDITIONAL_FREE("조건부 무료"),
    PAID("유료");

    private final String description;

    public static DeliveryType from(Integer deliveryFee, Integer freeShippingConditions) {
        if (deliveryFee == null || deliveryFee == 0) {
            return FREE;
        }
        if (freeShippingConditions != null) {
            return CONDITIONAL_FREE;
        }
        return PAID;
    }
}
