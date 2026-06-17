package com.bbangle.bbangle.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.member.MemberFixture;
import com.bbangle.bbangle.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("[단위 테스트] CartItem")
class CartItemUnitTest {

    @Test
    @DisplayName("소비자 장바구니 상품 추가에 성공한다.")
    void success_create_cartItem() {

        // given
        Member member = MemberFixture.defaultMember();
        Board board = BoardFixture.defaultBoard();

        // when
        CartItem cartItem = CartItem.create(member, board);

        // then
        assertThat(cartItem.getMember()).isEqualTo(member);
        assertThat(cartItem.getOptions()).isNotNull();
        assertThat(cartItem.getOptions()).isEmpty();
        assertThat(cartItem.getItem()).isEqualTo(board);
        assertThat(cartItem.getRequest()).isNull();
    }

    @Test
    @DisplayName("소비자 장바구니 상품 주문 요구사항 변경에 성공한다.")
    void changeRequest() {

        // given
        String request = "숟가락 1개 추가해주세요.";
        CartItem cartItem = CartItem.create(MemberFixture.defaultMember(), BoardFixture.defaultBoard());

        // when
        cartItem.changeRequest(request);

        // then
        assertThat(cartItem.getRequest()).isEqualTo(request);
    }

    @Nested
    @DisplayName("addOption() 테스트")
    class AddOptionTest {

        @Test
        @DisplayName("소비자 장바구니 상품 옵션 추가에 성공한다.")
        void success_addOption() {

            // given
            Member member = MemberFixture.defaultMember();
            Board board = BoardFixture.defaultBoard();
            Product product = ProductFixture.createWithStock(board, "옵션", 100);

            CartItem cartItem = CartItem.create(member, board);

            // when
            cartItem.addOption(product, 3);

            // then
            assertThat(cartItem.getOptions()).hasSize(1);

            CartOption cartOption = cartItem.getOptions().get(0);

            assertThat(cartOption.getCartItem()).isEqualTo(cartItem);
            assertThat(cartOption.getOption()).isEqualTo(product);
            assertThat(cartOption.getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("상품 옵션 수량이 0 이하이면 예외가 발생한다.")
        void fail_addOption_invalidQuantity() {

            // given
            Board board = BoardFixture.defaultBoard();
            CartItem cartItem = CartItem.create(MemberFixture.defaultMember(), board);
            Product product = ProductFixture.createWithStock(board, "옵션", 100);

            // when & then
            assertThatThrownBy(() -> cartItem.addOption(product, 0))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_REQUEST_QUANTITY);
                });
        }
    }
}