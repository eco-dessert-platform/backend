package com.bbangle.bbangle.order.dto;


import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "요일")
public enum DayOfWeek {
    @Schema(description = "월요일") MONDAY("월요일"),
    @Schema(description = "화요일") TUESDAY("화요일"),
    @Schema(description = "수요일") WEDNESDAY("수요일"),
    @Schema(description = "목요일") THURSDAY("목요일"),
    @Schema(description = "금요일") FRIDAY("금요일"),
    @Schema(description = "토요일") SATURDAY("토요일"),
    @Schema(description = "일요일") SUNDAY("일요일");

    @JsonValue
    private final String description;

    DayOfWeek(String description) {
        this.description = description;
    }

}