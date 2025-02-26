package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.orders.ProductDtoAtBoardDetail;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Nutrient {

    private Integer sugars;
    private Integer protein;
    private Integer carbohydrates;
    private Integer fat;
    private Integer weight;
    private Integer calories;

    public static Nutrient from(Product product) {
        return Nutrient.builder()
            .sugars(product.getNutrition().getSugars())
            .protein(product.getNutrition().getProtein())
            .carbohydrates(product.getNutrition().getCarbohydrates())
            .fat(product.getNutrition().getFat())
            .weight(product.getNutrition().getWeight())
            .calories(product.getNutrition().getCalories())
            .build();
    }

    public static Nutrient from(ProductDtoAtBoardDetail product) {
        return Nutrient.builder()
            .sugars(product.getSugars())
            .protein(product.getProtein())
            .carbohydrates(product.getCarbohydrates())
            .fat(product.getFat())
            .weight(product.getWeight())
            .calories(product.getCalories())
            .build();
    }

}
