package com.bbangle.bbangle.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.fixture.member.MemberFixture;
import com.bbangle.bbangle.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] Cart")
class CartUnitTest {

    @Test
    @DisplayName("소비자 장바구니 생성에 성공한다.")
    void success_create_cart() {

        // given
        Member member = MemberFixture.defaultMember();

        // when
        Cart cart = Cart.create(member);

        // then
        assertThat(cart.getMember()).isEqualTo(member);
        assertThat(cart.getCartItems()).isNotNull();
        assertThat(cart.getCartItems()).isEmpty();
    }
}