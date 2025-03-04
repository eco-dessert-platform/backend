package com.bbangle.bbangle.fixturemonkey;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.PropertyAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class FixtureMonkeyTest extends AbstractIntegrationTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardRepository boardRepository;

    @RepeatedTest(3)
    @DisplayName("엔티티의 하나의 속성인 리스트의 사이즈는 1이상이다")
    void test1() {
        Member member = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(Member.class);
        assertThat(member.getWishListFolders()).size().isGreaterThanOrEqualTo(1);
        assertThat(member.getWishListStores()).size().isGreaterThanOrEqualTo(1);
        assertThat(member.getWithdrawals()).size().isGreaterThanOrEqualTo(1);
    }


    @RepeatedTest(3)
    @DisplayName("양방향일 때 데이터 정합성을 맞춘다")
    void test2() {
        Member member = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(Member.class);
        assertThat(member.getWishListFolders().get(0).getMember().getId()).isEqualTo(member.getId());
        assertThat(member.getWishListStores().get(0).getMember().getId()).isEqualTo(member.getId());
        assertThat(member.getWithdrawals().get(0).getMember().getId()).isEqualTo(member.getId());
    }

    @RepeatedTest(3)
    @DisplayName("List<자식엔티티>의 Id는 서로 다르다")
    void test3() {
        // Given
        Member member = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(Member.class);
        memberRepository.save(member);

        // When
        List<WishListFolder> wishListFolders = member.getWishListFolders();

        // Then
        Set<Long> uniqueIds = new HashSet<>();
        for (WishListFolder folder : wishListFolders) {
            Long id = folder.getId();
            assertThat(id).isNotNull();
            assertThat(uniqueIds).doesNotContain(id);
            uniqueIds.add(id);
        }
    }

    @RepeatedTest(3)
    @DisplayName("product 없이 board 생성시 순환참조에 걸리지 않는다.")
    void test4() {
        Board board = fixtureBoard(Map.of("title", "testTitle"));
        fixtureBoardDetail(Map.of("board", board));
    }

    @RepeatedTest(3)
    @DisplayName("product 있이 board 생성시 순환참조에 걸리지 않는다.")
    void test5() {
        Product product = fixtureProduct(Map.of());
        Board board = fixtureBoard(Map.of("products", List.of(product)));
    }


    @Test
    @DisplayName("각 Board마다 고유한 BaordStatic을 만든다.")
    void test6() {
        for (int i = 1; i <= 15; i++) {
            Board board = boardRepository.save(fixtureBoard(Map.of()));
            System.out.println("boardId: " + board.getId() + ", title: " + board.getTitle());
            System.out.println("boardstaticid " + board.getBoardStatistic().getId());
            System.out.println("boardstatic score" + board.getBoardStatistic().getBasicScore());
        }
    }

    @RepeatedTest(3)
    @DisplayName("fixtureBoard 파라미터로 store을 넘겨도 된다.")
    void test7() {
        Store store = fixtureStore(Map.of());
        boardRepository.save(fixtureBoard(Map.of("store", store)));
    }

    @Test
    @DisplayName("Product 저장 후 Board에 주입 시 product 식별자 변경 예외가 발생한다")
    void testProductIdentifierAlteredException() {
        // given
        Product product = fixtureProduct(Map.of("title", "testTitle"));
        Product savedProduct = productRepository.save(product);
        Board board = fixtureBoard(Map.of("products", List.of(savedProduct)));

        // when
        JpaSystemException exception = assertThrows(JpaSystemException.class, () -> {
            Board savedBoard = boardRepository.save(board);
            List<Product> products = productRepository.findAll();
        });

        // then
        assertTrue(exception.getMessage()
                .contains("identifier of an instance of com.bbangle.bbangle.board.domain.Product was altered"));
    }

    @Test
    @DisplayName("Product의 저장은 Cascade 설정이 된 Board로 이루어져야한다.")
    void testProductSavedThroughBoardCascade() {
        Product product = fixtureProduct(Map.of("title", "testTitle"));
        Board board = fixtureBoard(Map.of("products", List.of(product)));

        Board savedBoard = boardRepository.save(board);

        assertEquals(1, savedBoard.getProducts().size());
        assertEquals("testTitle", savedBoard.getProducts().get(0).getTitle());
    }


    @Test
    @DisplayName("Board 저장하고 Product의 board를 세팅하고 저장하면 product.board.BoardDetail id에 접근하면서 오류가 발생한다.")
    void test10() {
        Board board = fixtureBoard(Map.of());
        Product product = fixtureProduct(Map.of("board", board));

        assertThatThrownBy(() -> boardRepository.save(board))
                .isInstanceOf(JpaSystemException.class);
    }



}
