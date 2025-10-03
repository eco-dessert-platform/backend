package com.bbangle.bbangle.board.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영양 정보")
public record NutritionInfoRequest(
    @Schema(description = "총 중량 (g)", example = "250")
    int totalWeight,

    @Schema(description = "1회 제공량 (g)", example = "125")
    int servingSize,

    @Schema(description = "탄수화물 함량 (g)", example = "20")
    int carbohydrates,

    @Schema(description = "당류 함량 (g)", example = "5")
    int sugars,

    @Schema(description = "단백질 함량 (g)", example = "30")
    int protein,

    @Schema(description = "지방 함량 (g)", example = "15")
    int fat,

    @Schema(description = "칼로리 (kcal)", example = "350")
    int calories
) {
}