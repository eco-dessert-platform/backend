package com.bbangle.bbangle.board.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EditStockFlag {
    INCREASE("재고 증가"),
    DECREASE("재고 감소"),
    SOLDOUT("재고 품절처리");

    private final String description;
}
