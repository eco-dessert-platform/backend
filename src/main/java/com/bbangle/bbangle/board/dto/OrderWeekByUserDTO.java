package com.bbangle.bbangle.board.dto;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record OrderWeekByUserDTO(
    Boolean monday,
    Boolean tuesday,
    Boolean wednesday,
    Boolean thursday,
    Boolean friday,
    Boolean saturday,
    Boolean sunday
) {
    public static OrderWeekByUserDTO from(String day) {
        if (Objects.isNull(day) || day.trim().isEmpty()) {
            return empty();
        }

        OrderWeekByUserDTOBuilder builder = OrderWeekByUserDTO.builder()
            .monday(false)
            .thursday(false)
            .wednesday(false)
            .thursday(false)
            .friday(false)
            .saturday(false)
            .sunday(false);

        if (day.contains("MONDAY")) {
            builder.monday(true);
        }

        if (day.contains("TUESDAY")) {
            builder.monday(true);
        }

        if (day.contains("WEDNESDAY")) {
            builder.monday(true);
        }

        if (day.contains("THURSDAY")) {
            builder.monday(true);
        }

        if (day.contains("FRIDAY")) {
            builder.monday(true);
        }

        if (day.contains("SATURDAY")) {
            builder.monday(true);
        }

        if (day.contains("SUNDAY")) {
            builder.monday(true);
        }

        return builder.build();
    }

    public static OrderWeekByUserDTO empty() {
        return OrderWeekByUserDTO.builder()
            .monday(false)
            .tuesday(false)
            .wednesday(false)
            .thursday(false)
            .friday(false)
            .saturday(false)
            .sunday(false)
            .build();
    }
}
