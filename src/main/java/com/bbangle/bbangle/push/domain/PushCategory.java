package com.bbangle.bbangle.push.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PushCategory {
    BBANGCKETING("빵켓팅"),
    RESTOCK("재입고");

    private final String description;
}
