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
@Schema(description = "상품 수정 요청 DTO")
public class UpdateProductRequest {

    @Schema(description = "상품 ID (null이면 신규 상품)", example = "1")
    private Long productId;

    @Schema(description = "상품 카테고리")
    private String category;

    @Schema(description = "상품명", example = "비건 쿠키")
    private String title;

    @Schema(description = "성분 카테고리")
    private DietaryTagsRequest dietaryTags;

    @Schema(description = "게시글 가격에 추가되는 금액", example = "1000")
    private int plusPriceWithBoardPrice;

    @Schema(description = "재고 수량", example = "100")
    private int stock;

    @Schema(description = "주문 가능 요일 정보")
    private AvailabilityRequest availability;

    @Schema(description = "영양 정보")
    private NutritionInfoRequest nutritionInfo;
}
