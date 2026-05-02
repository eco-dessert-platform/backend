package com.bbangle.bbangle.board.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SaleStatus {

    ON_SALE("판매중"),
    OUT_OF_STOCK("품절"),
    STOPPED("판매중지"),
    PENDING("판매대기"),
    BANNED("판매금지");

    private final String description;
}
