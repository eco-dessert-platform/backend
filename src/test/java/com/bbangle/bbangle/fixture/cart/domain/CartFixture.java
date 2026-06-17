package com.bbangle.bbangle.fixture.cart.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.member.domain.Member;

public class CartFixture {

    private CartFixture() {}

    public static Cart defaultCartItem(Member member, Board board) {
        return Cart.builder()
            .member(member)
            .item(board)
            .build();
    }
}
