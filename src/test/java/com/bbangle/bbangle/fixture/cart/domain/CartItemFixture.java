package com.bbangle.bbangle.fixture.cart.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartItem;
import org.springframework.test.util.ReflectionTestUtils;

public class CartItemFixture {

    private CartItemFixture() {}

    public static CartItem defaultCartItem(Cart cart, Board board) {
        return CartItem.builder()
            .cart(cart)
            .item(board)
            .build();
    }

    public static CartItem withId(CartItem cartItem, Long id) {
        ReflectionTestUtils.setField(cartItem, "id", id);
        return cartItem;
    }
}
