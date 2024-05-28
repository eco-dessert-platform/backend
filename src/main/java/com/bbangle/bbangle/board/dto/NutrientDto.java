package com.bbangle.bbangle.board.dto;

import java.util.Random;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NutrientDto {
    Integer sugars;
    Integer protein;
    Integer carbohydrates;
    Integer fat;
    Integer weight;
    Integer calories;

    // fixed: 프론트 API 연동 테스트를 위한 값입니다. 해당 기능 구현 시 삭제됩니다.
    public NutrientDto toFixture() {
        Random random = new Random();

        return NutrientDto.builder()
            .sugars(random.nextInt(0, 100))
            .protein(random.nextInt(0, 100))
            .carbohydrates(random.nextInt(0, 100))
            .fat(random.nextInt(0, 100))
            .weight(random.nextInt(0, 100))
            .calories(random.nextInt(0, 100))
            .build();
    }
}
