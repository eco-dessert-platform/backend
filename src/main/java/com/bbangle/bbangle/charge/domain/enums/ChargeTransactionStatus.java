package com.bbangle.bbangle.charge.domain.enums;

public enum ChargeTransactionStatus {
    PENDING("대기"),
    COMPLETED("완료");

    private final String description;

    ChargeTransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
