package com.bbangle.bbangle.charge.domain.enums;

public enum ChargeCategory {
    ACCUMULATE("적립"),
    DEDUCT("소진");

    private final String description;

    ChargeCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
