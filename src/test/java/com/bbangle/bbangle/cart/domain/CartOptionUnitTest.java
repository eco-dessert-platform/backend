package com.bbangle.bbangle.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.cart.domain.CartFixture;
import com.bbangle.bbangle.fixture.cart.domain.CartOptionFixture;
import com.bbangle.bbangle.fixture.member.MemberFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("[단위 테스트] Cart")
class CartOptionUnitTest {

    @Nested
    @DisplayName("create() 테스트")
    class CreateTest {

        Cart cart = CartFixture.defaultCartItem(MemberFixture.defaultMember(), BoardFixture.defaultBoard());
        Product option = ProductFixture.defaultProductWithStore(StoreFixture.defaultStore());

        @Test
        @DisplayName("장바구니에 상품 옵션이 추가된다.")
        void success_create_cartOption() {

            // given
            int quantity = 3;

            // when
            CartOption cartOption = CartOption.create(cart, option, quantity);

            // then
            assertThat(cartOption.getCart()).isEqualTo(cart);
            assertThat(cartOption.getOption()).isEqualTo(option);
            assertThat(cartOption.getQuantity()).isEqualTo(quantity);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("수량이 0이하일 경우 장바구니에 상품 옵션 생성에 실패한다.")
        void fail_create_cartOption(int quantity) {

            // when & then
            assertThatThrownBy(() -> CartOption.create(cart, option, quantity)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getMessage()).isEqualTo(BbangleErrorCode.INVALID_REQUEST_QUANTITY.getMessage());
                });
        }
    }

    @Nested
    @DisplayName("updateQuantity() 테스트")
    class UpdateQuantityTest {

        Cart cart = CartFixture.defaultCartItem(MemberFixture.defaultMember(), BoardFixture.defaultBoard());
        Product option = ProductFixture.defaultProductWithStore(StoreFixture.defaultStore());

        @ParameterizedTest
        @ValueSource(ints = {0, 5, 100})
        @DisplayName("장바구니 옵션 수량 변경에 성공한다.")
        void success_updateQuantity(int quantity) {

            // given
            CartOption cartOption = CartOptionFixture.defaultCartOption(cart, option, 0);

            // when
            cartOption.updateQuantity(quantity);

            // then
            assertThat(cartOption.getQuantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("수량을 음수로 변경하면 실패한다.")
        void fail_update_quantity() {

            // given
            CartOption cartOption = CartOptionFixture.defaultCartOption(cart, option, 0);

            // when & then
            assertThatThrownBy(() -> cartOption.updateQuantity(-1)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getMessage()).isEqualTo(BbangleErrorCode.INVALID_REQUEST_QUANTITY.getMessage());
                });
        }
    }
}