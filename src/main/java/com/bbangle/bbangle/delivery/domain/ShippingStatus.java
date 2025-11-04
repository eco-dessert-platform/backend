package com.bbangle.bbangle.delivery.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShippingStatus {

    READY("배송 준비중"),
    IN_TRANSIT("배송중"),
    OUT_FOR_DELIVERY("배달 출발"),
    DELIVERED("배송 완료"),
    RETURN_REQUESTED("반품 요청"),
    RETURNED("반품 완료"),
    CANCELED("배송 취소");

    private final String description;

}
