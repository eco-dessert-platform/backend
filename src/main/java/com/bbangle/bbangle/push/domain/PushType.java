package com.bbangle.bbangle.push.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PushType {
    @JsonProperty("DATE")
    DATE("날짜"),
    @JsonProperty("WEEK")
    WEEK("요일");

    private final String description;
}
