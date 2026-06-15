package com.bbangle.bbangle.fixture.cart.domain;

import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.member.domain.Member;

public class CartFixture {

    private CartFixture() {}

    public static Cart defaultCart(Member member) {
        return Cart.builder()
            .member(member)
            .build();
    }
}
