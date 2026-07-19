package com.bbangle.bbangle.cart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.TestContainersConfig;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.cart.domain.Cart;
import com.bbangle.bbangle.cart.domain.CartItem;
import com.bbangle.bbangle.config.QueryDslConfig;
import com.bbangle.bbangle.fixture.board.domain.BoardFixture;
import com.bbangle.bbangle.fixture.member.MemberFixture;
import com.bbangle.bbangle.fixture.store.domain.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.search.repository.component.SearchFilter;
import com.bbangle.bbangle.search.repository.component.SearchSort;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
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

@DisplayName("[슬라이스 테스트] CartItemRepository")
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
class CartItemRepositoryTest {

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
    EntityManager em;

    @Nested
    @DisplayName("findCartItemsByMember() 테스트")
    class FindCartItemsByMemberTest {

        private Member testMember;
        private Member otherMember;

        private Store testStore;
        private Board testBoard;

        @BeforeEach
        void setUp() {

            testMember = memberRepository.save(MemberFixture.defaultMember());
            otherMember = memberRepository.save(MemberFixture.createMemberWithName("other"));

            testStore = storeRepository.save(StoreFixture.defaultStore());
            testBoard = boardRepository.save(BoardFixture.defaultBoardWithStore(testStore, "상품명"));

            Cart cart = cartRepository.save(Cart.create(testMember));
            Cart otherCart = cartRepository.save(Cart.create(otherMember));

            cartItemRepository.save(CartItem.create(cart, testBoard));
            cartItemRepository.save(CartItem.create(otherCart, testBoard));

            em.flush();
            em.clear();
        }

        @Test
        @DisplayName("회원의 장바구니 상품만 조회한다.")
        void success_findCartItemsByMember() {

            // when
            List<CartItem> result = cartItemRepository.findCartItemsByMember(testMember);

            // then
            assertThat(result).hasSize(1);

            CartItem cartItem = result.get(0);
            assertThat(cartItem.getCart().getMember().getId()).isEqualTo(testMember.getId());
        }

        @Test
        @DisplayName("Item과 Store를 fetch join 하여 조회한다.")
        void success_findCartItemsByMember_fetchJoinItemAndStore() {

            // when
            List<CartItem> result = cartItemRepository.findCartItemsByMember(testMember);

            em.clear();

            // then
            assertThat(result).hasSize(1);

            CartItem cartItem = result.get(0);

            // fetch join 검증
            String itemName = cartItem.getItem().getTitle();
            String storeName = cartItem.getItem().getStore().getName();

            assertThat(itemName).isNotNull();
            assertThat(storeName).isNotNull();
        }
    }
}