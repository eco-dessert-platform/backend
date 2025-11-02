package com.bbangle.bbangle.board.customer.dto;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class OrderWeekByUserDTO {

    Boolean monday;
    Boolean tuesday;
    Boolean wednesday;
    Boolean thursday;
    Boolean friday;
    Boolean saturday;
    Boolean sunday;

    public static OrderWeekByUserDTO from(String day) {
        if (Objects.isNull(day) || day.trim().isEmpty()) {
            return empty();
        }

        OrderWeekByUserDTOBuilder builder = OrderWeekByUserDTO.builder()
            .monday(false)
            .tuesday(false)
            .wednesday(false)
            .thursday(false)
            .friday(false)
            .saturday(false)
            .sunday(false);

        if (day.contains("MONDAY")) {
            builder.monday(true);
        }

        if (day.contains("TUESDAY")) {
            builder.tuesday(true);
        }

        if (day.contains("WEDNESDAY")) {
            builder.wednesday(true);
        }

        if (day.contains("THURSDAY")) {
            builder.thursday(true);
        }

        if (day.contains("FRIDAY")) {
            builder.friday(true);
        }

        if (day.contains("SATURDAY")) {
            builder.saturday(true);
        }

        if (day.contains("SUNDAY")) {
            builder.sunday(true);
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
