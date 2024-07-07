package com.bbangle.bbangle.push.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PushCategory {
    BBANGCKETING("입고"),
    RESTOCK("재입고");

    private final String description;
}
