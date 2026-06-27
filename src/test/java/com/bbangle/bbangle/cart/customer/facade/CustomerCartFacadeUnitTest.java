package com.bbangle.bbangle.cart.customer.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.bbangle.bbangle.board.customer.service.BoardService;
import com.bbangle.bbangle.board.customer.service.ProductService;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest;
import com.bbangle.bbangle.cart.customer.service.CustomerCartItemService;
import com.bbangle.bbangle.cart.customer.service.CustomerCartOptionService;
import com.bbangle.bbangle.cart.customer.service.CustomerCartService;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.domain.CartOption;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.cart.domain.CartFixture;
import com.bbangle.bbangle.fixture.cart.domain.CartItemFixture;
import com.bbangle.bbangle.fixture.member.MemberFixture;
import com.bbangle.bbangle.member.customer.service.MemberService;
import com.bbangle.bbangle.member.domain.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("[단위테스트] CustomerCartFacade")
@ExtendWith(MockitoExtension.class)
class CustomerCartFacadeUnitTest {

    @InjectMocks
    private CustomerCartFacade customerCartFacade;

    @Mock
    private CustomerCartService customerCartService;

    @Mock
    private CustomerCartItemService customerCartItemService;

    @Mock
    private CustomerCartOptionService customerCartOptionService;

    @Mock
    private MemberService memberService;

    @Mock
    private BoardService boardService;

    @Mock
    private ProductService productService;

    @Nested
    @DisplayName("addCartItem() 테스트")
    class AddCartItemTest {

        Long memberId = 1L;
        Long boardId = 1L;
        Member member = MemberFixture.defaultMember();
        Cart cart = CartFixture.defaultCart(member);
        Board board = BoardFixture.defaultBoard();
        Product product = mock(Product.class);

        CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
            boardId,
            List.of(
                new CartRequest.AddCartRequest.SelectedOptions(
                    1L,
                    3
                )
            )
        );

        @Test
        @DisplayName("장바구니 상품이 없으면 새로 생성한다.")
        void success_addCartItem_create_cartItem() {

            // given
            CartItem cartItem = CartItemFixture.defaultCartItem(cart, board);

            given(memberService.findById(memberId)).willReturn(member);
            given(customerCartService.findCartByMember(member)).willReturn(cart);
            given(boardService.getBoard(boardId)).willReturn(board);
            given(customerCartItemService.findCartItem(cart, board)).willReturn(Optional.empty());
            given(customerCartItemService.createCartItem(cart, board)).willReturn(cartItem);
            given(customerCartOptionService.findAllByCartItem(cartItem)).willReturn(List.of());
            given(product.getId()).willReturn(1L);
            given(productService.findAllByIds(anyList())).willReturn(List.of(product));

            // when
            customerCartFacade.addCartItem(memberId, request);

            // then
            verify(customerCartItemService).createCartItem(cart, board);
        }

        @Test
        @DisplayName("장바구니 옵션이 없으면 새로 생성한다.")
        void success_addCartItem_create_cartOption() {

            // given
            CartItem cartItem = CartItemFixture.defaultCartItem(cart, board);

            given(memberService.findById(memberId)).willReturn(member);
            given(customerCartService.findCartByMember(member)).willReturn(cart);
            given(boardService.getBoard(boardId)).willReturn(board);
            given(customerCartItemService.findCartItem(cart, board)).willReturn(Optional.of(cartItem));
            given(customerCartOptionService.findAllByCartItem(cartItem)).willReturn(List.of());
            given(product.getId()).willReturn(1L);
            given(productService.findAllByIds(anyList())).willReturn(List.of(product));

            // when
            customerCartFacade.addCartItem(memberId, request);

            // then
            verify(product).validateStock(3);
            verify(customerCartOptionService).createCartOption(cartItem, product, 3);
        }

        @Test
        @DisplayName("장바구니 옵션이 이미 존재하면 수량을 증가시킨다.")
        void success_addCartItem_update_carOption_quantity() {

            // given
            CartItem cartItem = CartItemFixture.defaultCartItem(cart, board);
            CartOption cartOption = mock(CartOption.class);

            given(cartOption.getQuantity()).willReturn(2);
            given(cartOption.getOption()).willReturn(product);
            given(product.getId()).willReturn(1L);
            given(memberService.findById(memberId)).willReturn(member);
            given(customerCartService.findCartByMember(member)).willReturn(cart);
            given(boardService.getBoard(boardId)).willReturn(board);
            given(customerCartItemService.findCartItem(cart, board)).willReturn(Optional.of(cartItem));
            given(customerCartOptionService.findAllByCartItem(cartItem)).willReturn(List.of(cartOption));
            given(productService.findAllByIds(anyList())).willReturn(List.of(product));

            // when
            customerCartFacade.addCartItem(memberId, request);

            // then
            verify(product).validateStock(5);
            verify(customerCartOptionService).updateQuantity(cartOption, 5);
        }

        @Test
        @DisplayName("요청에 중복된 옵션이 존재하면 예외가 발생한다.")
        void fail_addCartItem_when_duplicate_option_exists() {

            // given
            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                board.getId(),
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(1L, 1),
                    new CartRequest.AddCartRequest.SelectedOptions(1L, 2)
                )
            );

            // when & then
            assertThatThrownBy(() -> customerCartFacade.addCartItem(memberId, request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.DUPLICATED_PRODUCT_OPTION);
                });

            verifyNoInteractions(memberService);
        }

        @Test
        @DisplayName("요청한 상품 옵션이 존재하지 않으면 예외가 발생한다.")
        void fail_addCartItem_product_notFound() {

            // given
            given(memberService.findById(memberId)).willReturn(member);
            given(customerCartService.findCartByMember(member)).willReturn(cart);
            given(boardService.getBoard(boardId)).willReturn(board);
            given(customerCartItemService.findCartItem(cart, board)).willReturn(Optional.empty());
            given(productService.findAllByIds(anyList())).willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> customerCartFacade.addCartItem(memberId, request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.PRODUCT_NOT_FOUND);
                });
        }
    }

    @Nested
    @DisplayName("deleteCartOptions() 테스트")
    class DeleteCartOptionsTest {

        Long memberId = 1L;
        CartItem cartItem = mock(CartItem.class);
        CartOption cartOption = mock(CartOption.class);

        @Test
        @DisplayName("선택한 옵션을 삭제한다.")
        void success_deleteCartOptions() {

            // given
            CartRequest.DeleteCartOptionsRequest request = new CartRequest.DeleteCartOptionsRequest(List.of(1L));

            given(cartOption.getId()).willReturn(1L);
            given(cartItem.getOptions()).willReturn(new ArrayList<>(List.of(cartOption)));
            given(cartItem.hasNoOptions()).willReturn(false);
            given(customerCartItemService.findAllWithOptionsByMemberIdAndOptionIds(memberId, request.cartOptionIds()))
                .willReturn(List.of(cartItem));

            // when
            customerCartFacade.deleteCartOptions(memberId, request);

            // then
            then(cartItem).should().removeOptions(Set.of(1L));
        }

        @Test
        @DisplayName("옵션 삭제 후 CartItem에 남은 옵션이 없으면 CartItem도 삭제한다.")
        void success_deleteCartOptions_cleanup_empty_cartItem() {

            // given
            CartRequest.DeleteCartOptionsRequest request = new CartRequest.DeleteCartOptionsRequest(List.of(1L));

            given(cartOption.getId()).willReturn(1L);
            given(cartItem.getOptions()).willReturn(new ArrayList<>(List.of(cartOption)));
            given(cartItem.hasNoOptions()).willReturn(true);
            given(customerCartItemService.findAllWithOptionsByMemberIdAndOptionIds(memberId, request.cartOptionIds()))
                .willReturn(List.of(cartItem));

            // when
            customerCartFacade.deleteCartOptions(memberId, request);

            // then
            then(cartItem).should().removeOptions(Set.of(1L));
            then(customerCartItemService).should().delete(cartItem);
        }

        @Test
        @DisplayName("존재하지 않는 옵션 ID가 포함되면 예외가 발생한다.")
        void fail_deleteCartOptions_option_not_found() {

            // given
            CartRequest.DeleteCartOptionsRequest request = new CartRequest.DeleteCartOptionsRequest(List.of(1L, 2L));

            given(customerCartItemService.findAllWithOptionsByMemberIdAndOptionIds(memberId, request.cartOptionIds()))
                .willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> customerCartFacade.deleteCartOptions(memberId, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.NOT_FOUND_CART_OPTION);
                });
        }

        @Test
        @DisplayName("다른 회원의 옵션은 조회되지 않아 NOT_FOUND로 처리된다.")
        void fail_deleteCartOptions_other_member_option() {

            // given
            CartRequest.DeleteCartOptionsRequest request = new CartRequest.DeleteCartOptionsRequest(List.of(1L));

            given(customerCartItemService.findAllWithOptionsByMemberIdAndOptionIds(memberId, request.cartOptionIds()))
                .willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> customerCartFacade.deleteCartOptions(memberId, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.NOT_FOUND_CART_OPTION);
                });
        }
    }
}