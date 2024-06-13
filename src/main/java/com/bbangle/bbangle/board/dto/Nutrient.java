package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Product;
import java.util.Random;
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
            .sugars(product.getSugars())
            .protein(product.getProtein())
            .carbohydrates(product.getCarbohydrates())
            .fat(product.getFat())
            .weight(product.getWeight())
            .calories(product.getCalories())
            .build();
    }

}
