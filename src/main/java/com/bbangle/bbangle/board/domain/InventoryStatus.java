package com.bbangle.bbangle.board.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InventoryStatus {

    IN_STOCK("재고 있음"),
    PARTIAL_SOLDOUT("부분 품절"),
    SOLDOUT("품절");

    private final String description;
}
