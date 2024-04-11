package com.bbangle.bbangle.testutil.model;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.wishListBoard.domain.WishlistProduct;
import com.bbangle.bbangle.wishListFolder.domain.WishlistFolder;

public class TestWishlistBoard extends TestModel<WishlistProduct>{
    private Long id = null;
    private WishlistFolder wishlistFolder;
    private Board board;
    private Long memberId;


    public TestWishlistBoard(WishlistFolder wishlistFolder){
        this.wishlistFolder = wishlistFolder;
    }

    public TestWishlistBoard setId(Long id) {
        this.id = id;

        return this;
    }

    public TestWishlistBoard setBoard(Board board) {
        this.board = board;

        return this;
    }

    public TestWishlistBoard setMemberId(Long memberId) {
        this.memberId = memberId;

        return this;
    }

    @Override
    public WishlistProduct getModel(){
        return WishlistProduct.builder()
                .id(id)
                .wishlistFolder(wishlistFolder)
                .board(board)
                .memberId(memberId)
                .build();
    }
}
