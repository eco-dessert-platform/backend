package com.bbangle.bbangle.model;

import java.util.Arrays;

public enum RedisEnum {
    STORE("store"),
    BOARD("bread"),
    BEST_KEYWORD("bestKeyword");

    private final String label;

    RedisEnum(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
