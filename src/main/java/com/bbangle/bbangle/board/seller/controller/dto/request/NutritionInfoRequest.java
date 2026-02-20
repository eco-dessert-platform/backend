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
@Schema(description = "영양 정보")
public class NutritionInfoRequest {

    @Schema(description = "총 중량 (g)", example = "250")
    private int totalWeight;

    @Schema(description = "1회 제공량 (g)", example = "125")
    private int servingSize;

    @Schema(description = "탄수화물 함량 (g)", example = "20")
    private int carbohydrates;

    @Schema(description = "당류 함량 (g)", example = "5")
    private int sugars;

    @Schema(description = "단백질 함량 (g)", example = "30")
    private int protein;

    @Schema(description = "지방 함량 (g)", example = "15")
    private int fat;

    @Schema(description = "칼로리 (kcal)", example = "350")
    private int calories;
}
