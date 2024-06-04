package com.bbangle.bbangle.review.domain;

public enum Badge {
    GOOD("맛있어요"),
    BAD("아쉬워요"),
    SWEET("달아요"),
    PLAIN("담백해요"),
    SOFT("부드러워요"),
    DRY("퍽퍽해요");

    private final String action;

    Badge(String action) {
        this.action = action;
    }

    public String action(){
        return action;
    }
}
