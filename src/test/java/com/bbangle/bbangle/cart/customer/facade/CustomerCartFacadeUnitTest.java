package com.bbangle.bbangle.cart.customer.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.bbangle.bbangle.board.customer.service.BoardService;
import com.bbangle.bbangle.board.customer.service.ProductService;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest;
import com.bbangle.bbangle.cart.customer.service.CustomerCartOptionService;
import com.bbangle.bbangle.cart.customer.service.CustomerCartService;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartOption;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.cart.domain.CartFixture;
import com.bbangle.bbangle.fixture.member.MemberFixture;
import com.bbangle.bbangle.member.customer.service.MemberService;
import com.bbangle.bbangle.member.domain.Member;
import java.util.List;
import java.util.Optional;
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
    private CustomerCartOptionService customerCartOptionService;

    @Mock
    private MemberService memberService;

    @Mock
    private BoardService boardService;

    @Mock
    private ProductService productService;

    @Nested
    @DisplayName("addCartItem() 테스트")
    class AddCartTest {

        Long memberId = 1L;
        Long boardId = 1L;
        Member member = MemberFixture.defaultMember();
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
            Cart cart = CartFixture.defaultCartItem(member, board);

            given(memberService.findById(memberId)).willReturn(member);
            given(boardService.getBoard(boardId)).willReturn(board);
            given(customerCartService.findCartByMemberAndBoard(member, board)).willReturn(Optional.empty());
            given(customerCartService.createCart(member, board)).willReturn(cart);
            given(customerCartOptionService.findAllByCart(cart)).willReturn(List.of());
            given(product.getId()).willReturn(1L);
            given(productService.findAllByIds(anyList())).willReturn(List.of(product));

            // when
            customerCartFacade.addCartItem(memberId, request);

            // then
            verify(customerCartService).createCart(member, board);
            assertThat(cart.getOptions()).hasSize(1);
        }

        @Test
        @DisplayName("장바구니 옵션이 없으면 새로 생성한다.")
        void success_addCartItem_create_cartOption() {

            // given
            Cart cart = CartFixture.defaultCartItem(member, board);

            given(memberService.findById(memberId)).willReturn(member);
            given(boardService.getBoard(boardId)).willReturn(board);
            given(customerCartService.findCartByMemberAndBoard(member, board)).willReturn(Optional.of(cart));
            given(customerCartOptionService.findAllByCart(cart)).willReturn(List.of());
            given(product.getId()).willReturn(1L);
            given(productService.findAllByIds(anyList())).willReturn(List.of(product));

            // when
            customerCartFacade.addCartItem(memberId, request);

            // then
            verify(product).validateStock(3);

            assertThat(cart.getOptions()).hasSize(1);

            CartOption option = cart.getOptions().get(0);

            assertThat(option.getOption()).isEqualTo(product);
            assertThat(option.getQuantity()).isEqualTo(3);
        }

        @Test
        @DisplayName("장바구니 옵션이 이미 존재하면 수량을 증가시킨다.")
        void success_addCartItem_update_carOption_quantity() {

            // given
            Cart cart = CartFixture.defaultCartItem(member, board);
            CartOption cartOption = mock(CartOption.class);

            given(cartOption.getQuantity()).willReturn(2);
            given(cartOption.getOption()).willReturn(product);
            given(product.getId()).willReturn(1L);
            given(memberService.findById(memberId)).willReturn(member);
            given(boardService.getBoard(boardId)).willReturn(board);
            given(customerCartService.findCartByMemberAndBoard(member, board)).willReturn(Optional.of(cart));
            given(customerCartOptionService.findAllByCart(cart)).willReturn(List.of(cartOption));
            given(productService.findAllByIds(anyList())).willReturn(List.of(product));

            // when
            customerCartFacade.addCartItem(memberId, request);

            // then
            verify(product).validateStock(5);
            verify(customerCartOptionService).updateQuantity(cartOption, 5);
        }

        @Test
        @DisplayName("기존 옵션은 수량 증가하고 신규 옵션은 추가한다.")
        void success_addCartItem_mixed_options() {

            // given
            Product product1 = mock(Product.class);
            Product product2 = mock(Product.class);
            CartOption existingOption = mock(CartOption.class);
            Cart cart = CartFixture.defaultCartItem(member, board);

            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                boardId,
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(1L, 3),
                    new CartRequest.AddCartRequest.SelectedOptions(2L, 1)
                )
            );

            given(existingOption.getQuantity()).willReturn(2);
            given(existingOption.getOption()).willReturn(product1);
            given(product1.getId()).willReturn(1L);
            given(product2.getId()).willReturn(2L);
            given(memberService.findById(memberId)).willReturn(member);
            given(boardService.getBoard(boardId)).willReturn(board);
            given(customerCartService.findCartByMemberAndBoard(member, board)).willReturn(Optional.of(cart));
            given(customerCartOptionService.findAllByCart(cart)).willReturn(List.of(existingOption));
            given(productService.findAllByIds(anyList())).willReturn(List.of(product1, product2));

            // when
            customerCartFacade.addCartItem(memberId, request);

            // then
            verify(customerCartOptionService).updateQuantity(existingOption, 5);

            assertThat(cart.getOptions()).hasSize(1);
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
            given(boardService.getBoard(boardId)).willReturn(board);
            given(customerCartService.findCartByMemberAndBoard(member, board)).willReturn(Optional.empty());
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

        @Test
        @DisplayName("상품 옵션이 다른 상품에 속하면 예외가 발생한다.")
        void fail_addCartItem_product_not_belongs_to_board() {

            // given
            Cart cart = CartFixture.defaultCartItem(member, board);

            given(memberService.findById(memberId)).willReturn(member);
            given(boardService.getBoard(boardId)).willReturn(board);
            given(customerCartService.findCartByMemberAndBoard(member, board)).willReturn(Optional.of(cart));
            given(product.getId()).willReturn(1L);
            given(productService.findAllByIds(anyList())).willReturn(List.of(product));

            willThrow(new BbangleException(BbangleErrorCode.PRODUCT_NOT_FOUND)).given(product).validateBelongsTo(board);

            // when & then
            assertThatThrownBy(() -> customerCartFacade.addCartItem(memberId, request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.PRODUCT_NOT_FOUND);
                });
        }

        @Test
        @DisplayName("장바구니에 추가할 상품 옵션의 수량보다 재고가 부족하면 예외가 발생한다.")
        void fail_addCartItem_insufficient_stock() {

            // given
            Cart cart = CartFixture.defaultCartItem(member, board);

            given(memberService.findById(memberId)).willReturn(member);
            given(boardService.getBoard(boardId)).willReturn(board);
            given(customerCartService.findCartByMemberAndBoard(member, board)).willReturn(Optional.of(cart));
            given(product.getId()).willReturn(1L);
            given(productService.findAllByIds(anyList())).willReturn(List.of(product));

            willThrow(new BbangleException(BbangleErrorCode.INVALID_REQUEST_STOCK)).given(product).validateStock(3);

            // when & then
            assertThatThrownBy(() -> customerCartFacade.addCartItem(memberId, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_REQUEST_STOCK);
                });
        }

        @Test
        @DisplayName("기존 장바구니의 상품 옵션 수량 증가 시 재고가 부족하면 예외가 발생한다.")
        void fail_addCartItem_update_quantity_insufficient_stock() {

            // given
            Cart cart = CartFixture.defaultCartItem(member, board);
            CartOption cartOption = mock(CartOption.class);

            given(cartOption.getQuantity()).willReturn(2);
            given(cartOption.getOption()).willReturn(product);
            given(product.getId()).willReturn(1L);
            given(memberService.findById(memberId)).willReturn(member);
            given(boardService.getBoard(boardId)).willReturn(board);
            given(customerCartService.findCartByMemberAndBoard(member, board)).willReturn(Optional.of(cart));
            given(customerCartOptionService.findAllByCart(cart)).willReturn(List.of(cartOption));
            given(productService.findAllByIds(anyList())).willReturn(List.of(product));

            willThrow(new BbangleException(BbangleErrorCode.INVALID_REQUEST_STOCK)).given(product).validateStock(5);

            // when & then
            assertThatThrownBy(() -> customerCartFacade.addCartItem(memberId, request))
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode())
                        .isEqualTo(BbangleErrorCode.INVALID_REQUEST_STOCK);
                });

            verify(customerCartOptionService, never()).updateQuantity(any(), anyInt());
        }
    }
}