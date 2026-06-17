package com.bbangle.bbangle.fixture.cart.domain;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartOption;

public class CartOptionFixture {

    private CartOptionFixture() {}

    public static CartOption defaultCartOption(
        Cart cart,
        Product option,
        int quantity
    ) {
        return CartOption.builder()
            .cart(cart)
            .option(option)
            .quantity(quantity)
            .build();
    }
}
