package com.bbangle.bbangle.board.domain;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Category {
    ALL("전체"),
    ALL_BREAD("빵 전체"),
    ALL_SNACK("간식 전체"),
    BREAD("식빵/모닝빵"),
    COOKIE("쿠키/비스킷/크래커"),
    BAGEL("베이글/도넛"),
    CAKE("케이크"),
    TART("타르트/파이"),
    SNACK("과자"),
    JAM("잼/청"),
    ICE_CREAM("아이스크림"),
    YOGURT("요거트"),
    GRANOLA("그래놀라"),
    ETC("기타");

    private final String description;

    public static boolean checkCategory(String category) {
        return Arrays.stream(Category.values())
            .anyMatch(e -> e.name()
                .equals(category));
    }
}
