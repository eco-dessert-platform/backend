package com.bbangle.bbangle.push.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PushCategory {
    @JsonProperty("BBANGCKETING")
    BBANGCKETING("입고"),
    @JsonProperty("RESTOCK")
    RESTOCK("재입고");

    private final String description;
}
