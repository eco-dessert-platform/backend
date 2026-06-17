package com.bbangle.bbangle.fixture.cart.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.member.domain.Member;

public class CartItemFixture {

    private CartItemFixture() {}

    public static CartItem defaultCartItem(Member member, Board board) {
        return CartItem.builder()
            .member(member)
            .item(board)
            .build();
    }
}
