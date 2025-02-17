package com.bbangle.bbangle.board.dto;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Nutrition;
import com.bbangle.bbangle.board.domain.Product;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductRequest {
    private String title;
    private int plusPriceWithBoardPrice;
    private Category category;
    private int stock;

    private boolean glutenFreeTag;
    private boolean highProteinTag;
    private boolean sugarFreeTag;
    private boolean veganTag;
    private boolean ketogenicTag;

    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;

    private int totalWeight;
    private int servingSize;
    private int carbohydrates;
    private int sugars;
    private int protein;
    private int fat;
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
