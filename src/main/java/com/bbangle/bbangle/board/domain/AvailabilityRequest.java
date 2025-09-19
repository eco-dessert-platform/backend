package com.bbangle.bbangle.board.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 가능 요일 정보")
public record AvailabilityRequest(
    @Schema(description = "월요일 주문 가능 여부", example = "true")
    boolean monday,

    @Schema(description = "화요일 주문 가능 여부", example = "true")
    boolean tuesday,

    @Schema(description = "수요일 주문 가능 여부", example = "true")
    boolean wednesday,

    @Schema(description = "목요일 주문 가능 여부", example = "true")
    boolean thursday,

    @Schema(description = "금요일 주문 가능 여부", example = "true")
    boolean friday,

    @Schema(description = "토요일 주문 가능 여부", example = "true")
    boolean saturday,

    @Schema(description = "일요일 주문 가능 여부", example = "true")
    boolean sunday
) {
}