package com.bbangle.bbangle.claim.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaimDeliveryType {

    OUTBOUND("출고"),
    RETURN_PICKUP("반품 수거"),
    EXCHANGE_PICKUP("교환 수거"),
    EXCHANGE_REDELIVERY("교환 재배송");

    private final String description;

}
