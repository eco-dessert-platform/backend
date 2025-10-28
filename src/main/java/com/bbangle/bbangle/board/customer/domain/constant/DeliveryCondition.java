package com.bbangle.bbangle.board.customer.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeliveryCondition {
    PAID("유료"),
    FREE("무료"),
    CONDITIONAL_FREE("조건부 무료");

    private final String description;
}
