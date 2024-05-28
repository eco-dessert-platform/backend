package com.bbangle.bbangle.board.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderAvailableDate {

    String startDate;
    String endDate;

    // fixed: 프론트 API 연동 테스트를 위한 값입니다. 해당 기능 구현 시 삭제됩니다.
    public OrderAvailableDate toFixture() {
        return OrderAvailableDate.builder()
            .startDate("2024.05.29")
            .endDate("2024.08.13")
            .build();
    }
}
