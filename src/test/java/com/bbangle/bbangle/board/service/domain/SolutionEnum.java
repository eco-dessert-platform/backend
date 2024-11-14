package com.bbangle.bbangle.board.service.domain;

public enum SolutionEnum {
    태그("태그"),
    묶음상품("묶음상품"),
    품절여부("품절여부");

    private final String displayName;

    SolutionEnum(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
