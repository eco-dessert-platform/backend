package com.bbangle.bbangle.fixture.cart.domain;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.domain.CartOption;
import org.springframework.test.util.ReflectionTestUtils;

public class CartOptionFixture {

    private CartOptionFixture() {}

    public static CartOption defaultCartOption(
        CartItem cartItem,
        Product option,
        int quantity
    ) {
        return CartOption.builder()
            .cartItem(cartItem)
            .option(option)
            .quantity(quantity)
            .build();
    }

    public static CartOption withId(CartOption cartOption, Long id) {
        ReflectionTestUtils.setField(cartOption, "id", id);
        return cartOption;
    }
}
