package com.bbangle.bbangle.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.member.MemberFixture;
import com.bbangle.bbangle.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] CartItem")
class CartItemUnitTest {

    @Test
    @DisplayName("소비자 장바구니 상품 추가에 성공한다.")
    void success_create_cartItem() {

        // given
        Member member = MemberFixture.defaultMember();
        Cart cart = Cart.create(member);
        Board board = BoardFixture.defaultBoard();

        // when
        CartItem cartItem = CartItem.create(cart, board);

        // then
        assertThat(cartItem.getCart()).isEqualTo(cart);
        assertThat(cartItem.getOptions()).isNotNull();
        assertThat(cartItem.getOptions()).isEmpty();
        assertThat(cartItem.getItem()).isEqualTo(board);
    }
}