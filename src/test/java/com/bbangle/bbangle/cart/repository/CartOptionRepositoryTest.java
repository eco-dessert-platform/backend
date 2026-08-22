package com.bbangle.bbangle.cart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.board.repository.ProductRepository;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.cart.domain.CartOption;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.board.domain.ProductFixture;
import com.bbangle.bbangle.fixture.member.MemberFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("[슬라이스 테스트] CartOptionRepository")
@ActiveProfiles("test")
@Import({
    TestContainersConfig.class,
    QueryDslConfig.class,
    SearchFilter.class,
    SearchSort.class
})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class CartOptionRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    StoreRepository storeRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    CartOptionRepository cartOptionRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    EntityManager em;

    private CartItem cartItem;
    private Member member;
    private CartOption cartOption;

    @BeforeEach
    void setUp() {

        Store store = storeRepository.save(StoreFixture.defaultStore());
        Board board = boardRepository.save(BoardFixture.defaultBoardWithStore(store, "board"));

        member = memberRepository.save(MemberFixture.defaultMember());

        Cart cart = cartRepository.save(Cart.create(member));

        cartItem = cartItemRepository.save(CartItem.create(cart, board));

        Product option1 = productRepository.save(ProductFixture.createWithStock(board, "옵션1", 10));
        Product option2 = productRepository.save(ProductFixture.createWithStock(board, "옵션2", 10));

        cartOption = cartOptionRepository.save(CartOption.create(cartItem, option1, 5));
        cartOptionRepository.save(CartOption.create(cartItem, option2, 1));

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("findCartOptionsByCartItemIds() 테스트")
    class FindCartOptionsByCartItemIdsTest {

        @Test
        @DisplayName("CartItemIds에 해당하는 CartOption과 Product가 함께 조회된다.")
        void success_findCartOptionsByCartItemIds() {

            // given
            List<Long> cartItemIds = List.of(cartItem.getId());

            // when
            List<CartOption> result = cartOptionRepository.findCartOptionsByCartItemIds(cartItemIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(co -> co.getCartItem().getId()).containsOnly(cartItem.getId());
            assertThat(result).extracting(co -> co.getOption().getTitle()).containsExactlyInAnyOrder("옵션1", "옵션2");
        }
    }

    @Nested
    @DisplayName("findByIdAndMemberId() 테스트")
    class FindByIdAndMemberIdTest {

        @Test
        @DisplayName("회원의 CartOption을 Product와 함께 조회한다.")
        void success_findByIdAndMemberId() {

            // given
            Long cartOptionId = cartOption.getId();

            // when
            Optional<CartOption> result = cartOptionRepository.findByIdAndMemberId(cartOptionId, member.getId());

            // then
            assertThat(result).isPresent();

            CartOption found = result.get();
            assertThat(found.getId()).isEqualTo(cartOptionId);
            assertThat(found.getOption().getTitle()).isEqualTo("옵션1");

            PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();
            assertThat(util.isLoaded(found.getOption())).isTrue();
        }

        @Test
        @DisplayName("다른 회원의 CartOption이면 조회되지 않는다.")
        void fail_findByIdAndMemberId_invalidMemberId() {

            // given
            Member otherMember = memberRepository.save(MemberFixture.createMemberWithName("other@test.com"));

            // when
            Optional<CartOption> result = cartOptionRepository.findByIdAndMemberId(cartOption.getId(), otherMember.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 CartOption이면 조회되지 않는다.")
        void fail_findByIdAndMemberId_invalidCartOptionId() {

            // given
            Long invalidCartOptionId = 99999L;

            // when
            Optional<CartOption> result = cartOptionRepository.findByIdAndMemberId(invalidCartOptionId, member.getId());

            // then
            assertThat(result).isEmpty();
        }
    }
}