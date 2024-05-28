package com.bbangle.bbangle.review.dto;

import java.util.Random;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ReviewResponse {
    private Float rating;
    private Integer total;
    private Boolean badgeTaste;
    private Boolean badgeBrix;
    private Boolean badgeTexture;

    // 프론트 API 연동 테스트를 위한 값입니다. 해당 기능 구현 시 삭제됩니다.
    public ReviewResponse toFixture() {
        Random random = new Random();

        return ReviewResponse.builder()
            .rating(Math.round(random.nextFloat(0,5) * 100) / 100f)
            .total(random.nextInt(0,1000))
            .badgeTaste(random.nextBoolean())
            .badgeBrix(random.nextBoolean())
            .badgeTexture(random.nextBoolean())
            .build();
    }
}
