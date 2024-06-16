package com.bbangle.bbangle.fixture;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductFixture {

    public static Product veganFreeProduct(Board board) {
        String dishName = getProductTitle();
        List<Boolean> randomIngredient = getRandomIngredient(4);

        return Product.builder()
            .board(board)
            .title(dishName)
            .veganTag(true)
            .ketogenicTag(randomIngredient.get(0))
            .sugarFreeTag(randomIngredient.get(1))
            .highProteinTag(randomIngredient.get(2))
            .glutenFreeTag(randomIngredient.get(3))
            .category(Category.BREAD)
            .build();
    }

    public static Product ketogenicProduct(Board board) {
        String dishName = getProductTitle();
        List<Boolean> randomIngredient = getRandomIngredient(4);

        return Product.builder()
            .board(board)
            .title(dishName)
            .ketogenicTag(true)
            .veganTag(randomIngredient.get(0))
            .sugarFreeTag(randomIngredient.get(1))
            .highProteinTag(randomIngredient.get(2))
            .glutenFreeTag(randomIngredient.get(3))
            .category(Category.BREAD)
            .build();
    }

    public static Product randomProduct(Board board) {
        String dishName = getProductTitle();
        List<Boolean> productIngredient = getRandomIngredient(6);

        return Product.builder()
            .board(board)
            .title(dishName)
            .veganTag(productIngredient.get(0))
            .ketogenicTag(productIngredient.get(1))
            .sugarFreeTag(productIngredient.get(2))
            .highProteinTag(productIngredient.get(3))
            .glutenFreeTag(productIngredient.get(4))
            .category(Category.BREAD)
            .build();
    }

    public static Product gluetenFreeProduct(Board board) {
        String dishName = getProductTitle();
        List<Boolean> productIngredient = getRandomIngredient(5);

        return Product.builder()
            .board(board)
            .title(dishName)
            .sugarFreeTag(true)
            .veganTag(productIngredient.get(0))
            .ketogenicTag(productIngredient.get(1))
            .highProteinTag(productIngredient.get(2))
            .glutenFreeTag(productIngredient.get(3))
            .category(Category.BREAD)
            .build();
    }

    public static Product nonGluetenFreeProduct(Board board) {
        String dishName = getProductTitle();
        List<Boolean> productIngredient = getRandomIngredient(4);

        return Product.builder()
            .board(board)
            .title(dishName)
            .sugarFreeTag(false)
            .veganTag(productIngredient.get(0))
            .ketogenicTag(productIngredient.get(1))
            .highProteinTag(productIngredient.get(2))
            .glutenFreeTag(productIngredient.get(3))
            .category(Category.BREAD)
            .build();
    }

    public static Product nonVeganFreeProduct(Board board) {
        String dishName = getProductTitle();
        List<Boolean> randomIngredient = getRandomIngredient(4);

        return Product.builder()
            .board(board)
            .title(dishName)
            .veganTag(false)
            .ketogenicTag(randomIngredient.get(0))
            .sugarFreeTag(randomIngredient.get(1))
            .highProteinTag(randomIngredient.get(2))
            .category(Category.BREAD)
            .glutenFreeTag(randomIngredient.get(3))
            .build();
    }

    public static Product highProteinProduct(Board board) {
        String dishName = getProductTitle();
        List<Boolean> productIngredient = getRandomIngredient(4);

        return Product.builder()
            .board(board)
            .title(dishName)
            .highProteinTag(true)
            .veganTag(productIngredient.get(0))
            .ketogenicTag(productIngredient.get(1))
            .sugarFreeTag(productIngredient.get(2))
            .glutenFreeTag(productIngredient.get(3))
            .category(Category.BREAD)
            .build();
    }

    public static Product nonHighProteinProduct(Board board) {
        String dishName = getProductTitle();
        List<Boolean> randomIngredient = getRandomIngredient(4);

        return Product.builder()
            .board(board)
            .title(dishName)
            .highProteinTag(false)
            .ketogenicTag(randomIngredient.get(0))
            .sugarFreeTag(randomIngredient.get(1))
            .veganTag(randomIngredient.get(2))
            .category(Category.BREAD)
            .glutenFreeTag(randomIngredient.get(3))
            .build();
    }

    public static Product sugarFreeProduct(Board board) {
        String dishName = getProductTitle();
        List<Boolean> productIngredient = getRandomIngredient(4);

        return Product.builder()
            .board(board)
            .title(dishName)
            .sugarFreeTag(true)
            .veganTag(productIngredient.get(0))
            .ketogenicTag(productIngredient.get(1))
            .highProteinTag(productIngredient.get(2))
            .glutenFreeTag(productIngredient.get(3))
            .category(Category.BREAD)
            .build();
    }

    public static Product nonSugarFreeProduct(Board board) {
        String dishName = getProductTitle();
        List<Boolean> randomIngredient = getRandomIngredient(4);

        return Product.builder()
            .board(board)
            .title(dishName)
            .sugarFreeTag(false)
            .ketogenicTag(randomIngredient.get(0))
            .highProteinTag(randomIngredient.get(1))
            .veganTag(randomIngredient.get(2))
            .category(Category.BREAD)
            .glutenFreeTag(randomIngredient.get(3))
            .build();
    }


    public static Product categoryBasedProduct(Board board, Category category) {
        String dishName = getProductTitle();
        List<Boolean> randomIngredient = getRandomIngredient(5);

        return Product.builder()
            .board(board)
            .title(dishName)
            .veganTag(randomIngredient.get(0))
            .ketogenicTag(randomIngredient.get(1))
            .sugarFreeTag(randomIngredient.get(2))
            .highProteinTag(randomIngredient.get(3))
            .glutenFreeTag(randomIngredient.get(4))
            .category(category)
            .build();
    }

    public static Product categoryBasedWithSugarFreeProduct(Board board, Category category) {
        String dishName = getProductTitle();
        List<Boolean> randomIngredient = getRandomIngredient(4);

        return Product.builder()
            .board(board)
            .title(dishName)
            .sugarFreeTag(true)
            .veganTag(randomIngredient.get(0))
            .ketogenicTag(randomIngredient.get(1))
            .highProteinTag(randomIngredient.get(2))
            .glutenFreeTag(randomIngredient.get(3))
            .category(category)
            .build();
    }

    public static Product categoryBasedWithNonSugarFreeProduct(Board board, Category category) {
        String dishName = getProductTitle();
        List<Boolean> randomIngredient = getRandomIngredient(4);

        return Product.builder()
            .board(board)
            .title(dishName)
            .sugarFreeTag(false)
            .veganTag(randomIngredient.get(0))
            .ketogenicTag(randomIngredient.get(1))
            .highProteinTag(randomIngredient.get(2))
            .glutenFreeTag(randomIngredient.get(3))
            .category(category)
            .build();
    }

    public static Product productWithFullInfo(
        Board board,
        boolean glutenFreeTag,
        boolean highProteinTag,
        boolean sugarFreeTag,
        boolean veganTag,
        boolean ketogenicTag,
        Category category
    ) {
        String dishName = getProductTitle();
        int randomPrice = CommonFaker.faker.random()
            .nextInt(0, 10_000);

        return Product.builder()
            .board(board)
            .title(dishName)
            .price(randomPrice)
            .category(category)
            .glutenFreeTag(glutenFreeTag)
            .highProteinTag(highProteinTag)
            .sugarFreeTag(sugarFreeTag)
            .veganTag(veganTag)
            .ketogenicTag(ketogenicTag)
            .build();
    }

    private static String getProductTitle() {
        return CommonFaker.faker.food().dish();
    }

    private static List<Boolean> getRandomIngredient(int ingredientCount) {
        List<Boolean> productIngredient = new ArrayList<>();
        for (int i = 0; i < ingredientCount; i++) {
            int random = (int) (Math.random() + 1);
            boolean ingredient = random == 1;
            productIngredient.add(ingredient);
        }
        return productIngredient;
    }

}
