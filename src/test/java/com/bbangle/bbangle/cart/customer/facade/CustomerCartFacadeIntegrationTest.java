package com.bbangle.bbangle.cart.customer.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.cart.customer.controller.dto.CartRequest;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartOption;
import com.bbangle.bbangle.cart.repository.CartOptionRepository;
import com.bbangle.bbangle.cart.repository.CartRepository;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.member.MemberFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
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
    private CartOptionRepository cartOptionRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager em;

    @Nested
    @DisplayName("addCartItem() 테스트")
    class AddCartTest {

        @Test
        @DisplayName("장바구니 상품이 없으면 CartItem과 CartOption을 생성한다.")
        void success_addCartItem_create_cartItem() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Store store = storeRepository.save(StoreFixture.defaultStore());
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
            List<Cart> carts = cartRepository.findAll();
            List<CartOption> cartOptions = cartOptionRepository.findAll();

            assertThat(carts).hasSize(1);
            assertThat(cartOptions).hasSize(1);

            CartOption cartOption = cartOptions.get(0);

            assertThat(cartOption.getQuantity()).isEqualTo(3);
            assertThat(cartOption.getOption().getId()).isEqualTo(product.getId());

            assertThat(cartOptions.get(0).getCart().getId()).isEqualTo(carts.get(0).getId());
        }

        @Test
        @DisplayName("장바구니에 상품은 존재하지만 옵션이 없으면 CartOption을 생성한다.")
        void success_addCartItem_create_cartOption() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "옵션", 100));
            Cart original = cartRepository.save(Cart.create(member, board));

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
            List<Cart> carts = cartRepository.findAll();
            List<CartOption> cartOptions = cartOptionRepository.findAll();

            assertThat(carts).hasSize(1);
            assertThat(carts.get(0).getId()).isEqualTo(original.getId());
            assertThat(cartOptions).hasSize(1);
            assertThat(cartOptions.get(0).getQuantity()).isEqualTo(3);

            assertThat(cartOptions.get(0).getCart().getId()).isEqualTo(carts.get(0).getId());
            assertThat(cartOptions.get(0).getOption().getId()).isEqualTo(product.getId());
        }

        @Test
        @DisplayName("장바구니에 옵션이 이미 존재하면 수량을 증가시킨다.")
        void success_addCartItem_update_quantity() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "옵션", 100));
            Cart cart = cartRepository.save(Cart.create(member, board));
            CartOption cartOption = cartOptionRepository.save(CartOption.create(cart, product, 2));

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
            assertThat(cartRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("여러 상품 옵션을 한번에 장바구니에 추가한다.")
        void success_addCartItem_multiple_options() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product option1 = productRepository.save(ProductFixture.createWithStock(board, "옵션1", 100));
            Product option2 = productRepository.save(ProductFixture.createWithStock(board, "옵션2", 100));

            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                board.getId(),
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(option1.getId(), 2),
                    new CartRequest.AddCartRequest.SelectedOptions(option2.getId(), 3)
                )
            );

            // when
            customerCartFacade.addCartItem(member.getId(), request);

            em.flush();
            em.clear();

            // then
            List<CartOption> cartOptions = cartOptionRepository.findAll();

            assertThat(cartOptions).hasSize(2);
            assertThat(cartOptions.stream()
                .map(CartOption::getQuantity)
                .toList()
            ).containsExactlyInAnyOrder(2, 3);
        }

        @Test
        @DisplayName("기존 장바구니 상품 옵션은 유지되고 수량만 증가한다.")
        void success_addCartItem_keep_existing_option() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "옵션", 100));
            Cart cart = cartRepository.save(Cart.create(member, board));
            CartOption cartOption = cartOptionRepository.save(CartOption.create(cart, product, 2));

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
            List<CartOption> options = cartOptionRepository.findAll();

            assertThat(options).hasSize(1);
            assertThat(options.get(0).getId()).isEqualTo(cartOption.getId());
            assertThat(options.get(0).getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("기존 장바구니 옵션은 수량 증가하고 신규 옵션은 추가한다.")
        void success_addCartItem_mix_update_and_create() {

            // given
            Member member = memberRepository.save(MemberFixture.defaultMember());
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product1 = productRepository.save(ProductFixture.createWithStock(board, "옵션1", 100));
            Product product2 = productRepository.save(ProductFixture.createWithStock(board, "옵션2", 100));
            Cart cart = cartRepository.save(Cart.create(member, board));
            CartOption cartOption = cartOptionRepository.save(CartOption.create(cart, product1, 2));

            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                board.getId(),
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(product1.getId(), 3),
                    new CartRequest.AddCartRequest.SelectedOptions(product2.getId(), 3)
                )
            );

            // when
            customerCartFacade.addCartItem(member.getId(), request);

            em.flush();
            em.clear();

            // then
            List<CartOption> options = cartOptionRepository.findAll();

            assertThat(options).hasSize(2);
            assertThat(options.get(0).getId()).isEqualTo(cartOption.getId());
            assertThat(options.get(0).getQuantity()).isEqualTo(5);
            assertThat(options.get(1).getOption().getId()).isEqualTo(product2.getId());
            assertThat(options.get(1).getQuantity()).isEqualTo(3);
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
            Store store = storeRepository.save(StoreFixture.defaultStore());
            Board board = boardRepository.save(BoardFixture.bannedBoardWithStore(store, "상품명"));
            Product product = productRepository.save(ProductFixture.createWithStock(board, "옵션", 5));
            Cart cart = cartRepository.save(Cart.create(member, board));
            cartOptionRepository.save(CartOption.create(cart, product, 2));

            CartRequest.AddCartRequest request = new CartRequest.AddCartRequest(
                board.getId(),
                List.of(
                    new CartRequest.AddCartRequest.SelectedOptions(
                        product.getId(),
                        5
                    )
                )
            );

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
}