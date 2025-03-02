package com.bbangle.bbangle.board.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Nutrition {

    private Integer weight; // 총 중량 (g)

    private Integer servingWeight; // 1회 제공량 (g)

    private Integer carbohydrates; // 탄수화물 (g)

    private Integer sugars; // 당류 (g)

    private Integer protein; // 단백질 (g)

    private Integer fat; // 지방 (g)

    private Integer calories; // 칼로리 (kcal)
}

