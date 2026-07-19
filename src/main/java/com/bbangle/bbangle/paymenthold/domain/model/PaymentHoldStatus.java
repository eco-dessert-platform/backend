package com.bbangle.bbangle.paymenthold.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentHoldStatus {

    ON_HOLD("지급보류"),
    RELEASED("해제");

    private final String description;
}
