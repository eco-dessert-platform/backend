package com.bbangle.bbangle.board.seller.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 가능 요일 정보")
public class AvailabilityRequest {

    @Schema(description = "월요일 주문 가능 여부", example = "true")
    private boolean monday;

    @Schema(description = "화요일 주문 가능 여부", example = "true")
    private boolean tuesday;

    @Schema(description = "수요일 주문 가능 여부", example = "true")
    private boolean wednesday;

    @Schema(description = "목요일 주문 가능 여부", example = "true")
    private boolean thursday;

    @Schema(description = "금요일 주문 가능 여부", example = "true")
    private boolean friday;

    @Schema(description = "토요일 주문 가능 여부", example = "true")
    private boolean saturday;

    @Schema(description = "일요일 주문 가능 여부", example = "true")
    private boolean sunday;
}
