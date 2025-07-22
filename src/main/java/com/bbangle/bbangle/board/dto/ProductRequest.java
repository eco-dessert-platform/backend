package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Nutrition;
import com.bbangle.bbangle.board.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "상품 등록 요청 DTO")
public class ProductRequest {

    @Schema(description = "상품명", example = "비건 쿠키")
    private String title;

    @Schema(description = "게시글 가격에 추가되는 금액", example = "1000")
    private int plusPriceWithBoardPrice;

    @Schema(description = "상품 카테고리")
    private Category category;

    @Schema(description = "재고 수량", example = "100")
    private int stock;

    @Schema(description = "글루텐 프리 태그 여부", example = "true")
    private boolean glutenFreeTag;

    @Schema(description = "고단백 태그 여부", example = "true")
    private boolean highProteinTag;

    @Schema(description = "저당 태그 여부", example = "false")
    private boolean sugarFreeTag;

    @Schema(description = "비건 태그 여부", example = "false")
    private boolean veganTag;

    @Schema(description = "저지방 태그 여부(키토에서 변경됨)", example = "true")
    private boolean ketogenicTag;

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

    public Product toEntity(Board board) {
        return new Product(
                board,
                title,
                plusPriceWithBoardPrice,
                category,
                stock,
                glutenFreeTag,
                highProteinTag,
                sugarFreeTag,
                veganTag,
                ketogenicTag,
                monday,
                tuesday,
                wednesday,
                thursday,
                friday,
                saturday,
                sunday,
                new Nutrition(totalWeight, servingSize, carbohydrates,
                        sugars, protein, fat, calories)
        );
    }
}
