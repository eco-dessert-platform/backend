package com.bbangle.bbangle.board.seller.controller.dto.request;

import com.bbangle.bbangle.board.controller.dto.AvailabilityRequest;
import com.bbangle.bbangle.board.controller.dto.DietaryTagsRequest;
import com.bbangle.bbangle.board.controller.dto.NutritionInfoRequest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Nutrition;
import com.bbangle.bbangle.board.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 등록 요청 DTO")
public record ProductRequest_v2(
    @Schema(description = "상품 카테고리")
    Category category,

    @Schema(description = "상품명", example = "비건 쿠키")
    String title,

    @Schema(description = "성분 카테고리")
    DietaryTagsRequest dietaryTags,

    @Schema(description = "게시글 가격에 추가되는 금액", example = "1000")
    int plusPriceWithBoardPrice,

    @Schema(description = "재고 수량", example = "100")
    int stock,

    @Schema(description = "주문 가능 요일 정보")
    AvailabilityRequest availability,

    @Schema(description = "영양 정보")
    NutritionInfoRequest nutritionInfo
) {

    public Product toEntity(Board board) {
        return new Product(
            board,
            title,
            plusPriceWithBoardPrice,
            category,
            stock,
            dietaryTags.glutenFreeTag(),
            dietaryTags.highProteinTag(),
            dietaryTags.sugarFreeTag(),
            dietaryTags.veganTag(),
            dietaryTags.ketogenicTag(),
            availability.monday(),
            availability.tuesday(),
            availability.wednesday(),
            availability.thursday(),
            availability.friday(),
            availability.saturday(),
            availability.sunday(),
            new Nutrition(
                nutritionInfo.totalWeight(),
                nutritionInfo.servingSize(),
                nutritionInfo.carbohydrates(),
                nutritionInfo.sugars(),
                nutritionInfo.protein(),
                nutritionInfo.fat(),
                nutritionInfo.calories()
            )
        );
    }
}
