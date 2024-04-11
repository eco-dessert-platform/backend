package com.bbangle.bbangle.testutil.model;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Category;
import com.bbangle.bbangle.board.domain.Product;

public class TestProduct extends TestModel<Product>{
    Board board;
    String productName = "테스트 상품";
    Integer price = 3600;
    Category category = Category.ETC;
    Boolean glutenFreeTag = false;
    Boolean highProteinTag = false;
    Boolean sugarFreeTag = false;
    Boolean veganTag = false;
    Boolean ketogenicTag = false;

    public TestProduct(Board board){
        this.board = board;
    }

    public TestProduct setProductName(String productName) {
        this.productName = productName;

        return this;
    }

    public TestProduct setPrice(Integer price) {
        this.price = price;

        return this;
    }

    public TestProduct setCategory(Category category) {
        this.category = category;

        return this;
    }

    public TestProduct setGlutenFreeTag(Boolean glutenFreeTag) {
        this.glutenFreeTag = glutenFreeTag;

        return this;
    }

    public TestProduct setHighProteinTag(Boolean highProteinTag) {
        this.highProteinTag = highProteinTag;

        return this;
    }

    public TestProduct setSugarFreeTag(Boolean sugarFreeTag) {
        this.sugarFreeTag = sugarFreeTag;

        return this;
    }

    public TestProduct setVeganTag(Boolean veganTag) {
        this.veganTag = veganTag;

        return this;
    }

    public TestProduct setKetogenicTag(Boolean ketogenicTag) {
        this.ketogenicTag = ketogenicTag;

        return this;
    }

    @Override
    public Product getModel(){
        return Product.builder()
                .board(board)
                .title(productName)
                .price(price)
                .category(category)
                .glutenFreeTag(glutenFreeTag)
                .highProteinTag(highProteinTag)
                .sugarFreeTag(sugarFreeTag)
                .veganTag(veganTag)
                .ketogenicTag(ketogenicTag)
                .build();
    }
}
