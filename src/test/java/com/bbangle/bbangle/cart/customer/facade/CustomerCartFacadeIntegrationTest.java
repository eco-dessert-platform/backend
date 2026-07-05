package com.bbangle.bbangle.cart.customer.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductImgRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest;
import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest.UpdateCartOptionRequest;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.CartListResponse;
import com.bbangle.bbangle.cart.customer.controller.dto.CartResponse.UpdateCartOptionResponse;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.domain.CartOption;
import com.bbangle.bbangle.cart.repository.CartItemRepository;
import com.bbangle.bbangle.cart.repository.CartOptionRepository;
import com.bbangle.bbangle.cart.repository.CartRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductImgFixture;
import com.bbangle.bbangle.fixture.member.MemberFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[통합테스트] CustomerCartFacade")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustomerCartFacadeIntegrationTest {

    @Autowired
    private CustomerCartFacade customerCartFacade;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartOptionRepository cartOptionRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductImgRepository productImgRepository;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("addCartItem() 테스트")
    class AddCartItemTest {

        @Test
        @DisplayName("장바구니 상품이 없으면 CartItem과 CartOption을 생성한다.")
        void success_addCartItem_create_cartItem() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Store store = storeRepository.save(StoreFixture.defaultStore());
            cartRepository.save(Cart.create(member));
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "옵션", 100));

            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                board.getId(),
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(
                        product.getId(),
                        3
                    )
                )
            );

            // when
            customerCartFacade.addCartItem(member.getId(), request);

            em.flush();
            em.clear();

            // then
            List<CartItem> cartItems = cartItemRepository.findAll();
            List<CartOption> cartOptions = cartOptionRepository.findAll();

            assertThat(cartItems).hasSize(1);
            assertThat(cartOptions).hasSize(1);

            CartOption cartOption = cartOptions.get(0);

            assertThat(cartOption.getQuantity()).isEqualTo(3);
            assertThat(cartOption.getOption().getId()).isEqualTo(product.getId());

            assertThat(cartOptions.get(0).getCartItem().getId()).isEqualTo(cartItems.get(0).getId());
        }

        @Test
        @DisplayName("장바구니에 상품은 존재하지만 옵션이 없으면 CartOption을 생성한다.")
        void success_addCartItem_create_cartOption() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Cart cart = cartRepository.save(Cart.create(member));
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "옵션", 100));
            cartItemRepository.save(CartItem.create(cart, board));

            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                board.getId(),
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(
                        product.getId(),
                        3
                    )
                )
            );


            // when
            customerCartFacade.addCartItem(member.getId(), request);

            em.flush();
            em.clear();

            // then
            List<CartItem> cartItems = cartItemRepository.findAll();
            List<CartOption> cartOptions = cartOptionRepository.findAll();

            assertThat(cartItems).hasSize(1);
            assertThat(cartOptions).hasSize(1);
            assertThat(cartOptions.get(0).getQuantity()).isEqualTo(3);

            assertThat(cartOptions.get(0).getCartItem().getId()).isEqualTo(cartItems.get(0).getId());
            assertThat(cartOptions.get(0).getOption().getId()).isEqualTo(product.getId());
        }

        @Test
        @DisplayName("장바구니에 옵션이 이미 존재하면 수량을 증가시킨다.")
        void success_addCartItem_update_quantity() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Cart cart = cartRepository.save(Cart.create(member));
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "옵션", 100));
            CartItem cartItem = cartItemRepository.save(CartItem.create(cart, board));
            CartOption cartOption = cartOptionRepository.save(CartOption.create(cartItem, product, 2));

            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                board.getId(),
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(
                        product.getId(),
                        3
                    )
                )
            );

            // when
            customerCartFacade.addCartItem(member.getId(), request);

            em.flush();
            em.clear();

            // then
            CartOption updated = cartOptionRepository.findById(cartOption.getId()).orElseThrow();

            assertThat(updated.getQuantity()).isEqualTo(5);
            assertThat(cartOptionRepository.findAll()).hasSize(1);
            assertThat(cartItemRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("요청에 중복된 상품 옵션이 존재하면 예외가 발생한다.")
        void fail_addCartItem_duplicate_option() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));

            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                board.getId(),
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(1L, 1),
                    new CartRequest.AddCartRequest.SelectedOptions(1L, 2)
                )
            );

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> customerCartFacade.addCartItem(member.getId(), request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.DUPLICATED_PRODUCT_OPTION);
                });
        }

        @Test
        @DisplayName("존재하지 않는 상품 옵션이면 예외가 발생한다.")
        void fail_addCartItem_product_notFound() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            cartRepository.save(Cart.create(member));
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));

            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                board.getId(),
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(
                        99999L,
                        3
                    )
                )
            );

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> customerCartFacade.addCartItem(member.getId(), request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.PRODUCT_NOT_FOUND);
                });
        }

        @Test
        @DisplayName("다른 상품의 옵션을 장바구니에 추가하면 예외가 발생한다.")
        void fail_addCartItem_product_not_belongs_to_board() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            cartRepository.save(Cart.create(member));
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board1 = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명1"));
            Board board2 = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명2"));
            Product product = productRepository.save(ProductFixture.create(board2, "옵션"));

            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                board1.getId(),
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(
                        product.getId(),
                        1
                    )
                )
            );

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> customerCartFacade.addCartItem(member.getId(), request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.PRODUCT_NOT_FOUND);
                });
        }

        @Test
        @DisplayName("재고보다 많은 수량을 장바구니에 추가하면 예외가 발생한다.")
        void fail_addCartItem_insufficient_stock() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            cartRepository.save(Cart.create(member));
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "옵션", 5));

            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                board.getId(),
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(
                        product.getId(),
                        10
                    )
                )
            );

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> customerCartFacade.addCartItem(member.getId(), request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_REQUEST_STOCK);
                });
        }

        @Test
        @DisplayName("기존 수량 + 요청 수량이 재고를 초과하면 예외가 발생한다.")
        void fail_addCartItem_total_quantity_exceeds_stock() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Cart cart = cartRepository.save(Cart.create(member));
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "옵션", 5));
            CartItem cartItem = cartItemRepository.save(CartItem.create(cart, board));
            cartOptionRepository.save(CartOption.create(cartItem, product, 2));

            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                board.getId(),
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(
                        product.getId(),
                        5
                    )
                )
            );

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> customerCartFacade.addCartItem(member.getId(), request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_REQUEST_STOCK);
                });
        }
    }

    @Nested
    @DisplayName("deleteCartOptions() 테스트")
    class DeleteCartOptionsTest {

        @Test
        @DisplayName("선택한 옵션을 삭제한다.")
        void success_deleteCartOptions() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Cart cart = cartRepository.save(Cart.create(member));
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product1 = productRepository.save(ProductFixture.createWithStock(board, "옵션1", 100));
            Product product2 = productRepository.save(ProductFixture.createWithStock(board, "옵션2", 100));
            CartItem cartItem = cartItemRepository.save(CartItem.create(cart, board));
            CartOption option1 = cartOptionRepository.save(CartOption.create(cartItem, product1, 2));
            CartOption option2 = cartOptionRepository.save(CartOption.create(cartItem, product2, 3));

            CartRequest.DeleteCartOptionsRequest request = new CartRequest.DeleteCartOptionsRequest(
                List.of(option1.getId())
            );

            em.flush();
            em.clear();

            // when
            customerCartFacade.deleteCartOptions(member.getId(), request);

            em.flush();
            em.clear();

            // then
            assertThat(cartOptionRepository.findAll()).hasSize(1);
            assertThat(cartOptionRepository.findById(option2.getId())).isPresent();
            assertThat(cartItemRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("CartItem의 모든 옵션을 삭제하면 CartItem도 삭제된다.")
        void success_deleteCartOptions_cleanup_empty_cartItem() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Cart cart = cartRepository.save(Cart.create(member));
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "옵션", 100));
            CartItem cartItem = cartItemRepository.save(CartItem.create(cart, board));
            CartOption option = cartOptionRepository.save(CartOption.create(cartItem, product, 2));

            CartRequest.DeleteCartOptionsRequest request = new CartRequest.DeleteCartOptionsRequest(
                List.of(option.getId())
            );

            em.flush();
            em.clear();

            // when
            customerCartFacade.deleteCartOptions(member.getId(), request);

            em.flush();
            em.clear();

            // then
            assertThat(cartOptionRepository.findAll()).isEmpty();
            assertThat(cartItemRepository.findAll()).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 옵션 ID가 포함되면 예외가 발생한다.")
        void fail_deleteCartOptions_option_not_found() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            cartRepository.save(Cart.create(member));

            CartRequest.DeleteCartOptionsRequest request = new CartRequest.DeleteCartOptionsRequest(
                List.of(99999L)
            );

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> customerCartFacade.deleteCartOptions(member.getId(), request))
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
            Member member = memberRepository.save(MemberFixture.defaultMember());
            cartRepository.save(Cart.create(member));

            Member otherMember = memberRepository.save(
                Member.builder()
                    .providerId("other")
                    .provider(com.bbangle.bbangle.auth.oauth.OauthServerType.GOOGLE)
                    .email("other@test.com")
                    .phone("01099999999")
                    .name("other")
                    .nickname("other")
                    .birth("2000-01-01")
                    .profile("other.png")
                    .build()
            );
            Cart otherCart = cartRepository.save(Cart.create(otherMember));
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "옵션", 100));
            CartItem otherCartItem = cartItemRepository.save(CartItem.create(otherCart, board));
            CartOption otherOption = cartOptionRepository.save(CartOption.create(otherCartItem, product, 1));

            CartRequest.DeleteCartOptionsRequest request = new CartRequest.DeleteCartOptionsRequest(
                List.of(otherOption.getId())
            );

            em.flush();
            em.clear();

            // when & then
            assertThatThrownBy(() -> customerCartFacade.deleteCartOptions(member.getId(), request))
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

        private Member member;

        private Store store;
        private Board board;

        private CartItem cartItem;

        @BeforeEach
        void setUp() {

            member = memberRepository.save(MemberFixture.defaultMember());

            store = storeRepository.save(StoreFixture.defaultStore());
            board = boardRepository.save(BoardFixture.defaultBoardWithStore(store, "상품"));

            Cart cart = cartRepository.save(Cart.create(member));
            cartItem = cartItemRepository.save(CartItem.create(cart, board));

            Product option1 = productRepository.save(ProductFixture.createWithStock(board, "옵션1", 10));
            Product option2 = productRepository.save(ProductFixture.createWithStock(board, "옵션2", 20));

            productImgRepository.save(ProductImgFixture.defaultProductImgThumbnail(board, "thumbnail"));

            cartOptionRepository.save(CartOption.create(cartItem, option1, 5));
            cartOptionRepository.save(CartOption.create(cartItem, option2, 1));

            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("장바구니 정보를 Store 기준으로 그룹핑하여 반환한다")
        void success_getCart() {

            // when
            CartListResponse result = customerCartFacade.getCart(member.getId());

            // then
            assertThat(result.carts()).hasSize(1);

            CartListResponse.CartStoreDTO storeDto = result.carts().get(0);

            assertThat(storeDto.storeId()).isEqualTo(store.getId());
            assertThat(storeDto.storeName()).isEqualTo(store.getName());
            assertThat(storeDto.storeProfile()).isEqualTo(store.getProfile());

            assertThat(storeDto.items()).hasSize(1);

            CartListResponse.CartItemDTO itemDto = storeDto.items().get(0);

            assertThat(itemDto.cartItemId()).isEqualTo(cartItem.getId());
            assertThat(itemDto.itemId()).isEqualTo(board.getId());
            assertThat(itemDto.itemName()).isEqualTo(board.getTitle());
            assertThat(itemDto.price().base()).isEqualTo(board.getPrice());
            assertThat(itemDto.price().deliveryFee()).isEqualTo(board.getDeliveryFee());

            assertThat(itemDto.availableOptions())
                .extracting(CartListResponse.AvailableOptionDTO::optionName)
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
        @DisplayName("Store별로 CartItem을 그룹핑하여 반환한다")
        void success_getCart_multiStore() {

            // given
            Store store2 = storeRepository.save(StoreFixture.defaultStore("두번째 가게"));

            Board board2 = boardRepository.save(BoardFixture.defaultBoardWithStore(store, "상품2"));
            Board board3 = boardRepository.save(BoardFixture.defaultBoardWithStore(store2, "상품3"));

            CartItem cartItem2 = cartItemRepository.save(CartItem.create(cartItem.getCart(), board2));
            CartItem cartItem3 = cartItemRepository.save(CartItem.create(cartItem.getCart(), board3));

            Product option1 = productRepository.save(ProductFixture.createWithStock(board2, "옵션A", 10));
            Product option2 = productRepository.save(ProductFixture.createWithStock(board3, "옵션B", 20));

            productImgRepository.save(ProductImgFixture.defaultProductImgThumbnail(board2, "thumbnail2"));
            productImgRepository.save(ProductImgFixture.defaultProductImgThumbnail(board3, "thumbnail3"));

            cartOptionRepository.save(CartOption.create(cartItem2, option1, 1));
            cartOptionRepository.save(CartOption.create(cartItem3, option2, 2));

            em.flush();
            em.clear();

            // when
            CartListResponse result = customerCartFacade.getCart(member.getId());

            // then
            assertThat(result.carts()).hasSize(2);

            CartListResponse.CartStoreDTO firstStore = result.carts().stream()
                .filter(store -> store.storeId().equals(this.store.getId()))
                .findFirst()
                .orElseThrow();

            CartListResponse.CartStoreDTO secondStore = result.carts().stream()
                .filter(store -> store.storeId().equals(store2.getId()))
                .findFirst()
                .orElseThrow();

            assertThat(firstStore.items()).hasSize(2);
            assertThat(firstStore.items())
                .extracting(CartListResponse.CartItemDTO::itemName)
                .containsExactlyInAnyOrder("상품", "상품2");

            assertThat(secondStore.items()).hasSize(1);
            assertThat(secondStore.items().get(0).itemName()).isEqualTo("상품3");
        }

        @Test
        @DisplayName("장바구니가 비어있으면 빈 리스트를 반환한다")
        void success_getCart_empty() {

            // given
            Member emptyMember = memberRepository.save(MemberFixture.createMemberWithName("empty"));

            // when
            CartListResponse result = customerCartFacade.getCart(emptyMember.getId());

            // then
            assertThat(result.carts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateQuantity() 테스트")
    class UpdateQuantityTest {

        private Member member;
        private Member otherMember;
        private CartOption cartOption;
        private Product option;

        @BeforeEach
        void setUp() {

            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.defaultBoardWithStore(store, "board"));

            member = memberRepository.save(MemberFixture.defaultMember());
            otherMember = memberRepository.save(MemberFixture.createMemberWithName("other@test.com"));

            Cart cart = cartRepository.save(Cart.create(member));
            cartRepository.save(Cart.create(otherMember));

            CartItem cartItem = cartItemRepository.save(CartItem.create(cart, board));

            option = productRepository.save(ProductFixture.createWithStock(board, "옵션", 10));

            cartOption = cartOptionRepository.save(CartOption.create(cartItem, option, 2));

            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("장바구니 옵션의 수량을 변경한다.")
        void success_updateQuantity() {

            // given
            UpdateCartOptionRequest request = new UpdateCartOptionRequest(5);

            // when
            UpdateCartOptionResponse result = customerCartFacade.updateQuantity(member.getId(), cartOption.getId(), request);

            em.flush();
            em.clear();

            CartOption updatedCartOption = cartOptionRepository.findById(cartOption.getId()).orElseThrow();

            // then
            assertThat(result.cartOptionId()).isEqualTo(cartOption.getId());
            assertThat(result.optionId()).isEqualTo(option.getId());
            assertThat(result.optionName()).isEqualTo(option.getTitle());
            assertThat(result.quantity()).isEqualTo(5);
            assertThat(updatedCartOption.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("재고보다 많은 수량이면 예외가 발생한다.")
        void fail_updateQuantity_insufficientStock() {

            // given
            UpdateCartOptionRequest request = new UpdateCartOptionRequest(11);

            // when & then
            assertThatThrownBy(() ->
                customerCartFacade.updateQuantity(member.getId(), cartOption.getId(), request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.INVALID_REQUEST_STOCK);
                });
        }

        @Test
        @DisplayName("다른 회원의 장바구니 옵션이면 예외가 발생한다.")
        void fail_updateQuantity_notFoundCartOption() {

            // given
            UpdateCartOptionRequest request = new UpdateCartOptionRequest(5);

            // when & then
            assertThatThrownBy(() ->
                customerCartFacade.updateQuantity(otherMember.getId(), cartOption.getId(), request)
            )
                .isInstanceOf(BbangleException.class)
                .satisfies(e -> {
                    BbangleException ex = (BbangleException) e;
                    assertThat(ex.getBbangleErrorCode()).isEqualTo(BbangleErrorCode.NOT_FOUND_CART_OPTION);
                });
        }
    }
}