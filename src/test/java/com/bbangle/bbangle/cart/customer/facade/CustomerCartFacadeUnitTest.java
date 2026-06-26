package com.bbangle.bbangle.cart.customer.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.bbangle.bbangle.board.customer.service.BoardService;
import com.bbangle.bbangle.board.customer.service.ProductImgService;
import com.bbangle.bbangle.board.customer.service.ProductService;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.ProductImg;
import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.CartListResponse;
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

            Product thumbnailOption = ProductFixture.withId(ProductFixture.createWithStock(board, "옵션1", 10), 10000L);
            Product option2 = ProductFixture.withId(ProductFixture.createWithStock(board, "옵션2", 20), 10001L);

            ProductImg thumbnail = ProductImgFixture.defaultProductImgThumbnail(board, "thumbnail");

            CartOption cartOption1 = CartOptionFixture.withId(CartOptionFixture.defaultCartOption(cartItem, thumbnailOption, 5), 100000L);
            CartOption cartOption2 = CartOptionFixture.withId(CartOptionFixture.defaultCartOption(cartItem, option2, 1), 100001L);

            given(memberService.findById(member.getId())).willReturn(member);
            given(customerCartItemService.findAllByMember(member)).willReturn(List.of(cartItem));
            given(productImgService.findAllByBoardIds(List.of(board.getId()))).willReturn(List.of(thumbnail));
            given(productService.findAllByBoardIds(List.of(board.getId()))).willReturn(List.of(thumbnailOption, option2));
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
}