package com.bbangle.bbangle.board.dto;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record OrderDateByUserDTO(
    Boolean isBbangketing
) {
    public static OrderDateByUserDTO from(boolean bbangketingDate) {

        return OrderDateByUserDTO.builder()
            .isBbangketing(bbangketingDate)
            .build();
    }
}
