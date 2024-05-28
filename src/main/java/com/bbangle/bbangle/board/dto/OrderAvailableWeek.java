package com.bbangle.bbangle.board.dto;

import java.util.Random;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderAvailableWeek {
    Boolean monday;
    Boolean tuesday;
    Boolean wednesday;
    Boolean thursday;
    Boolean friday;
    Boolean saturday;
    Boolean sunday;

    // fixed: 프론트 API 연동 테스트를 위한 값입니다. 해당 기능 구현 시 삭제됩니다.
    public OrderAvailableWeek toFixture() {
        Random random = new Random();

        return OrderAvailableWeek.builder()
            .monday(random.nextBoolean())
            .tuesday(random.nextBoolean())
            .wednesday(random.nextBoolean())
            .thursday(random.nextBoolean())
            .friday(random.nextBoolean())
            .saturday(random.nextBoolean())
            .sunday(random.nextBoolean())
            .build();
    }
}
