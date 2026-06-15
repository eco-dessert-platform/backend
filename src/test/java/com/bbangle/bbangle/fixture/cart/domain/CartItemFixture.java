package com.bbangle.bbangle.fixture.cart.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartItem;

public class CartItemFixture {

    private CartItemFixture() {}

    public static CartItem defaultCartItem(Cart cart, Board board) {
        return CartItem.builder()
            .cart(cart)
            .item(board)
            .build();
    }
}
