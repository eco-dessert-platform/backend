package com.bbangle.bbangle.board.seller.service.command;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Nutrition;
import com.bbangle.bbangle.board.domain.Product;
import lombok.Builder;

@Builder
public record ProductCommand(
    String title,
    String category,
    int plusPriceWithBoardPrice,
    int stock,
    boolean glutenFreeTag,
    boolean highProteinTag,
    boolean sugarFreeTag,
    boolean veganTag,
    boolean ketogenicTag,
    boolean monday,
    boolean tuesday,
    boolean wednesday,
    boolean thursday,
    boolean friday,
    boolean saturday,
    boolean sunday,
    Nutrition nutrition
) {

    public Product toProduct(Board board) {
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
            nutrition
        );
    }
}
