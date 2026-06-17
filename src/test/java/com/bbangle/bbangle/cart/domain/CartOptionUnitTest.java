package com.bbangle.bbangle.cart.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.cart.domain.CartFixture;
import com.bbangle.bbangle.fixture.cart.domain.CartItemFixture;
import com.bbangle.bbangle.fixture.cart.domain.CartOptionFixture;
import com.bbangle.bbangle.fixture.member.MemberFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("[단위 테스트] CartOption")
class CartOptionUnitTest {

    @Nested
    @DisplayName("create() 테스트")
    class CreateTest {

        Cart cart = CartFixture.defaultCart(MemberFixture.defaultMember());
        CartItem cartItem = CartItemFixture.defaultCartItem(cart, BoardFixture.defaultBoard());
        Product option = ProductFixture.defaultProductWithStore(StoreFixture.defaultStore());

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 100, 999})
        @DisplayName("장바구니에 상품 옵션이 추가된다.")
        void success_create_cartOption(int quantity) {

            // when
            CartOption cartOption = CartOption.create(cartItem, option, quantity);

            // then
            assertThat(cartOption.getCartItem()).isEqualTo(cartItem);
            assertThat(cartOption.getOption()).isEqualTo(option);
            assertThat(cartOption.getQuantity()).isEqualTo(quantity);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5, 1000})
        @DisplayName("수량이 0이하이거나 1000이상일 경우 장바구니에 상품 옵션 생성에 실패한다.")
        void fail_create_cartOption(int quantity) {

            // when & then
            assertThatThrownBy(() -> CartOption.create(cartItem, option, quantity)
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

        Cart cart = CartFixture.defaultCart(MemberFixture.defaultMember());
        CartItem cartItem = CartItemFixture.defaultCartItem(cart, BoardFixture.defaultBoard());
        Product option = ProductFixture.defaultProductWithStore(StoreFixture.defaultStore());

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 100, 999})
        @DisplayName("장바구니 옵션 수량 변경에 성공한다.")
        void success_updateQuantity(int quantity) {

            // given
            CartOption cartOption = CartOptionFixture.defaultCartOption(cartItem, option, 1);

            // when
            cartOption.updateQuantity(quantity);

            // then
            assertThat(cartOption.getQuantity()).isEqualTo(quantity);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -100, 1000})
        @DisplayName("수량을 0 이하이거나 1000이상으로 변경하면 실패한다.")
        void fail_update_quantity(int quantity) {

            // given
            CartOption cartOption = CartOptionFixture.defaultCartOption(cartItem, option, 1);

            // when & then
            assertThatThrownBy(() -> cartOption.updateQuantity(quantity)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getMessage()).isEqualTo(BbangleErrorCode.INVALID_REQUEST_QUANTITY.getMessage());
                });
        }
    }
}