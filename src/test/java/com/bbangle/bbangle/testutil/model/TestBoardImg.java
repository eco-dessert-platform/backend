package com.bbangle.bbangle.testutil.model;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.ProductImg;

public class TestBoardImg extends TestModel<ProductImg> {
    private Board board;

    private String imageUrl = "/test.jpg";

    public TestBoardImg(Board board){
        this.board = board;
    }

    public TestBoardImg setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;

        return this;
    }


    @Override
    public ProductImg getModel(){
        return ProductImg.builder()
                .board(board)
                .url(imageUrl)
                .build();
    }
}
