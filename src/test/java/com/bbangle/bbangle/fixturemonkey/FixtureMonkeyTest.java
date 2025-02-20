package com.bbangle.bbangle.fixturemonkey;


import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.repository.BoardRepository;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.member.repository.MemberRepository;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class FixtureMonkeyTest extends AbstractIntegrationTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardRepository boardRepository;

    @RepeatedTest(10)
    @DisplayName("엔티티의 하나의 속성인 리스트의 사이즈는 1이상이다")
    void test1() {
        Member member = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(Member.class);
        assertThat(member.getWishListFolders()).size().isGreaterThanOrEqualTo(1);
        assertThat(member.getWishListStores()).size().isGreaterThanOrEqualTo(1);
        assertThat(member.getWithdrawals()).size().isGreaterThanOrEqualTo(1);
    }


    @RepeatedTest(10)
    @DisplayName("양방향일 때 데이터 정합성을 맞춘다")
    void test2() {
        Member member = FixtureMonkeyConfig.fixtureMonkey.giveMeOne(Member.class);
        assertThat(member.getWishListFolders().get(0).getMember().getId()).isEqualTo(member.getId());
        assertThat(member.getWishListStores().get(0).getMember().getId()).isEqualTo(member.getId());
        assertThat(member.getWithdrawals().get(0).getMember().getId()).isEqualTo(member.getId());
    }

    @RepeatedTest(10)
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

    @RepeatedTest(10)
    @DisplayName("product 없이 board 생성시 순환참조에 걸리지 않는다.")
    void test4() {
        Board board = fixtureBoard(Map.of());
    }

    @RepeatedTest(10)
    @DisplayName("product 있이 board 생성시 순환참조에 걸리지 않는다.")
    void test5() {
        Product product = fixtureProduct(Map.of());
        Board board = fixtureBoard(Map.of("products", List.of(product)));
    }

}
