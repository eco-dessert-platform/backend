package com.bbangle.bbangle.charge.domain.enums;

public enum ChargeReferenceType {
    SETTLEMENT("정산"),
    WITHDRAWAL("출금");

    private final String description;

    ChargeReferenceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
