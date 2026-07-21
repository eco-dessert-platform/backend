package com.bbangle.bbangle.cart.customer.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.bbangle.bbangle.board.customer.service.BoardService;
import com.bbangle.bbangle.board.customer.service.ProductImgService;
import com.bbangle.bbangle.board.customer.service.ProductService;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest;
import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest.UpdateCartOptionRequest;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.CartListResponse;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.UpdateCartOptionResponse;
import com.bbangle.bbangle.cart.customer.service.CustomerCartItemService;
import com.bbangle.bbangle.cart.customer.service.CustomerCartOptionService;
import com.bbangle.bbangle.cart.customer.service.CustomerCartService;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.domain.CartOption;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductImgFixture;
import com.bbangle.bbangle.fixture.cart.domain.CartFixture;
import com.bbangle.bbangle.fixture.cart.domain.CartItemFixture;
import com.bbangle.bbangle.fixture.cart.domain.CartOptionFixture;
import com.bbangle.bbangle.fixture.member.MemberFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.member.customer.service.MemberService;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.store.domain.Store;
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
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    private ProductImgService productImgService;

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

    @Nested
    @DisplayName("getCart() 테스트")
    class GetCartTest {

        @Test
        @DisplayName("장바구니 정보를 Store 기준으로 그룹핑하여 반환한다")
        void success_getCart() {

            // given
            Member member = MemberFixture.withId(MemberFixture.defaultMember(), 1L);

            Store store = StoreFixture.withId(StoreFixture.defaultStore(), 10L);
            Board board = BoardFixture.withId(BoardFixture.defaultBoardWithStore(store, "상품"), 100L);

            Cart cart = Cart.create(member);
            CartItem cartItem = CartItemFixture.withId(CartItemFixture.defaultCartItem(cart, board), 1000L);

            Product optionWithThumbnail = ProductFixture.withId(ProductFixture.createWithStock(board, "옵션1", 10), 10000L);
            Product option2 = ProductFixture.withId(ProductFixture.createWithStock(board, "옵션2", 20), 10001L);

            ProductImg thumbnail = ProductImgFixture.defaultProductImgThumbnail(board, "thumbnail");

            CartOption cartOption1 = CartOptionFixture.withId(CartOptionFixture.defaultCartOption(cartItem, optionWithThumbnail, 5), 100000L);
            CartOption cartOption2 = CartOptionFixture.withId(CartOptionFixture.defaultCartOption(cartItem, option2, 1), 100001L);

            given(memberService.findById(member.getId())).willReturn(member);
            given(customerCartItemService.findAllByMember(member)).willReturn(List.of(cartItem));
            given(productImgService.findAllByBoardIds(List.of(board.getId()))).willReturn(List.of(thumbnail));
            given(productService.findAllByBoardIds(List.of(board.getId()))).willReturn(List.of(optionWithThumbnail, option2));
            given(customerCartOptionService.findAllByCartItemIds(List.of(cartItem.getId())))
                .willReturn(List.of(cartOption1, cartOption2));

            // when
            CartListResponse result = customerCartFacade.getCart(member.getId());

            // then
            assertThat(result.carts()).hasSize(1);

            CartListResponse.CartStoreDTO storeDto = result.carts().get(0);

            assertThat(storeDto.storeId()).isEqualTo(store.getId());
            assertThat(storeDto.storeName()).isEqualTo(store.getName());
            assertThat(storeDto.items()).hasSize(1);

            CartListResponse.CartItemDTO itemDto = storeDto.items().get(0);

            assertThat(itemDto.itemId()).isEqualTo(board.getId());
            assertThat(itemDto.itemName()).isEqualTo(board.getTitle());
            assertThat(itemDto.itemImg()).isEqualTo(thumbnail.getUrl());

            assertThat(itemDto.availableOptions()).hasSize(2);
            assertThat(itemDto.selectedOptions()).hasSize(2);

            assertThat(itemDto.selectedOptions())
                .extracting(CartListResponse.SelectedOptionDTO::optionName)
                .containsExactlyInAnyOrder("옵션1", "옵션2");

            assertThat(itemDto.selectedOptions())
                .extracting(
                    CartListResponse.SelectedOptionDTO::optionName,
                    CartListResponse.SelectedOptionDTO::quantity
                )
                .containsExactlyInAnyOrder(
                    tuple("옵션1", 5),
                    tuple("옵션2", 1)
                );
        }

        @Test
        @DisplayName("장바구니가 비어있으면 빈 리스트를 반환한다")
        void success_getCart_empty() {

            // given
            Member member = MemberFixture.defaultMember();

            given(memberService.findById(member.getId())).willReturn(member);
            given(customerCartItemService.findAllByMember(member)).willReturn(List.of());

            // when
            CartListResponse result = customerCartFacade.getCart(member.getId());

            // then
            assertThat(result.carts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateQuantity() 테스트")
    class UpdateQuantityTest {

        @Test
        @DisplayName("장바구니 옵션의 수량을 변경한다.")
        void success_updateQuantity() {

            // given
            Long memberId = 1L;
            Long cartOptionId = 1L;
            UpdateCartOptionRequest request = new UpdateCartOptionRequest(3);

            Board board = mock(Board.class);
            Product option = ProductFixture.createWithStock(board, "옵션1", 10);
            ReflectionTestUtils.setField(option, "id", 1L);

            CartOption cartOption = mock(CartOption.class);

            given(customerCartOptionService.findByIdAndMemberId(memberId, cartOptionId)).willReturn(cartOption);
            given(cartOption.getId()).willReturn(cartOptionId);
            given(cartOption.getOption()).willReturn(option);
            given(cartOption.getQuantity()).willReturn(3);

            // when
            UpdateCartOptionResponse result = customerCartFacade.updateQuantity(memberId, cartOptionId, request);

            // then
            assertThat(result.cartOptionId()).isEqualTo(cartOptionId);
            assertThat(result.optionId()).isEqualTo(option.getId());
            assertThat(result.optionName()).isEqualTo(option.getTitle());
            assertThat(result.quantity()).isEqualTo(3);

            verify(customerCartOptionService).findByIdAndMemberId(memberId, cartOptionId);
            verify(customerCartOptionService).updateQuantity(cartOption, 3);
        }

        @Test
        @DisplayName("재고보다 많은 수량이면 예외가 발생한다.")
        void fail_updateQuantity_insufficientStock() {

            // given
            Long memberId = 1L;
            Long cartOptionId = 1L;
            UpdateCartOptionRequest request = new UpdateCartOptionRequest(11);

            Board board = mock(Board.class);
            Product option = ProductFixture.createWithStock(board, "옵션1", 10);
            CartOption cartOption = mock(CartOption.class);

            given(customerCartOptionService.findByIdAndMemberId(memberId, cartOptionId)).willReturn(cartOption);
            given(cartOption.getOption()).willReturn(option);

            // when & then
            assertThatThrownBy(() -> customerCartFacade.updateQuantity(memberId, cartOptionId, request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_REQUEST_STOCK);
                });

            verify(customerCartOptionService, never()).updateQuantity(any(), anyInt());
        }

        @Test
        @DisplayName("장바구니 옵션이 존재하지 않으면 예외가 발생한다.")
        void fail_updateQuantity_notFoundCartOption() {

            // given
            Long memberId = 1L;
            Long cartOptionId = 1L;
            UpdateCartOptionRequest request = new UpdateCartOptionRequest(3);

            given(customerCartOptionService.findByIdAndMemberId(memberId, cartOptionId))
                .willThrow(new BbangleException(BbangleErrorCode.NOT_FOUND_CART_OPTION));

            // when & then
            assertThatThrownBy(() -> customerCartFacade.updateQuantity(memberId, cartOptionId, request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.NOT_FOUND_CART_OPTION);
                });

            verify(customerCartOptionService, never()).updateQuantity(any(), anyInt());
        }
    }

    @Nested
    @DisplayName("changeOption() 테스트")
    class ChangeOptionTest {

        Long memberId = 1L;
        Long cartOptionId = 1L;
        Long newOptionId = 2L;
        CartRequest.ChangeCartOptionRequest request = new CartRequest.ChangeCartOptionRequest(newOptionId);

        CartOption cartOption = mock(CartOption.class);
        CartItem cartItem = mock(CartItem.class);
        Board board = mock(Board.class);
        Product newOption = mock(Product.class);

        @Test
        @DisplayName("장바구니 옵션을 다른 옵션으로 변경한다.")
        void success_changeOption() {

            // given
            given(customerCartOptionService.findByIdAndMemberId(memberId, cartOptionId)).willReturn(cartOption);
            given(cartOption.getCartItem()).willReturn(cartItem);
            given(cartOption.getQuantity()).willReturn(2);
            given(cartItem.getItem()).willReturn(board);
            given(cartItem.getOptions()).willReturn(List.of());
            given(productService.findAllByIds(List.of(newOptionId))).willReturn(List.of(newOption));

            // when
            customerCartFacade.changeOption(memberId, cartOptionId, request);

            // then
            then(newOption).should().validateBelongsTo(board);
            then(newOption).should().validateStock(2);
            then(customerCartOptionService).should().changeOption(cartOption, newOption);
        }

        @Test
        @DisplayName("변경하려는 옵션이 존재하지 않으면 예외가 발생한다.")
        void fail_changeOption_product_notFound() {

            // given
            given(customerCartOptionService.findByIdAndMemberId(memberId, cartOptionId)).willReturn(cartOption);
            given(cartOption.getCartItem()).willReturn(cartItem);
            given(cartItem.getItem()).willReturn(board);
            given(productService.findAllByIds(List.of(newOptionId))).willReturn(List.of());

            // when & then
            assertThatThrownBy(() -> customerCartFacade.changeOption(memberId, cartOptionId, request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.PRODUCT_NOT_FOUND);
                });

            then(customerCartOptionService).should(never()).changeOption(any(), any());
        }

        @Test
        @DisplayName("같은 상품에 이미 담긴 옵션으로 변경하려 하면 예외가 발생한다.")
        void fail_changeOption_duplicated() {

            // given
            CartOption existingOption = mock(CartOption.class);

            given(customerCartOptionService.findByIdAndMemberId(memberId, cartOptionId)).willReturn(cartOption);
            given(cartOption.getCartItem()).willReturn(cartItem);
            given(cartOption.getId()).willReturn(cartOptionId);
            given(cartItem.getItem()).willReturn(board);
            given(cartItem.getOptions()).willReturn(List.of(existingOption));
            given(existingOption.getId()).willReturn(99L);
            given(existingOption.getOption()).willReturn(newOption);
            given(newOption.getId()).willReturn(newOptionId);
            given(productService.findAllByIds(List.of(newOptionId))).willReturn(List.of(newOption));

            // when & then
            assertThatThrownBy(() -> customerCartFacade.changeOption(memberId, cartOptionId, request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.DUPLICATED_PRODUCT_OPTION);
                });

            then(customerCartOptionService).should(never()).changeOption(any(), any());
        }

        @Test
        @DisplayName("새 옵션의 재고가 부족하면 예외가 발생한다.")
        void fail_changeOption_insufficientStock() {

            // given
            given(customerCartOptionService.findByIdAndMemberId(memberId, cartOptionId)).willReturn(cartOption);
            given(cartOption.getCartItem()).willReturn(cartItem);
            given(cartOption.getQuantity()).willReturn(5);
            given(cartItem.getItem()).willReturn(board);
            given(cartItem.getOptions()).willReturn(List.of());
            given(productService.findAllByIds(List.of(newOptionId))).willReturn(List.of(newOption));
            willThrow(new BbangleException(BbangleErrorCode.INVALID_REQUEST_STOCK))
                .given(newOption).validateStock(5);

            // when & then
            assertThatThrownBy(() -> customerCartFacade.changeOption(memberId, cartOptionId, request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_REQUEST_STOCK);
                });

            then(customerCartOptionService).should(never()).changeOption(any(), any());
        }
    }
}