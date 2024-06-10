package com.bbangle.bbangle.board.repository.folder.query;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.sort.FolderBoardSortType;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.RankingFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.ranking.domain.BoardStatistic;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import com.querydsl.core.BooleanBuilder;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LowPriceBoardQueryProviderTest extends AbstractIntegrationTest {

    Member member;
    WishListFolder wishListFolder;
    Long firstSavedId;
    Long lastSavedId;
    Store store;

    @BeforeEach
    void generalSetUp() {
        member = MemberFixture.createKakaoMember();
        member = memberService.getFirstJoinedMember(member);
        store = StoreFixture.storeGenerator();
        store = storeRepository.save(store);

        wishListFolder = wishListFolderRepository.findByMemberId(member.getId())
            .stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("기본 폴더가 생성되어 있지 않아 테스트 실패"));

        for (int i = 0; i < 12; i++) {
            Board createdBoard = BoardFixture.randomBoardWithPrice(store, i * 1000);
            createdBoard = boardRepository.save(createdBoard);

            if (i == 0) {
                firstSavedId = createdBoard.getId();
            }
            if (i == 11) {
                lastSavedId = createdBoard.getId();
            }

            Product product = ProductFixture.randomProduct(createdBoard);
            Product product2 = ProductFixture.randomProduct(createdBoard);
            productRepository.save(product);
            productRepository.save(product2);
            BoardStatistic boardStatistic = RankingFixture.newRanking(createdBoard);
            boardStatisticRepository.save(boardStatistic);
            wishListBoardService.wish(member.getId(), createdBoard.getId(),
                new WishListBoardRequest(wishListFolder.getId()));
        }
    }

    @Test
    @DisplayName("정상적으로 낮은 가격순으로 폴더 안의 게시물을 조회한다.")
    void getBoardResponseDaoWithPopularOrder() {
        //given, when
        List<BoardResponseDao> boardResponseDaoList = new LowPriceBoardQueryProvider(
            queryFactory,
            new BooleanBuilder(),
            FolderBoardSortType.LOW_PRICE.getOrderSpecifier(),
            wishListFolder)
            .getBoards();

        //then
        assertThat(boardResponseDaoList).hasSize(22);

        int expectedPrice = 0;
        for (int i = 0; i < boardResponseDaoList.size() / 2; i++) {
            assertThat(boardResponseDaoList.get(i * 2).price()).isEqualTo(expectedPrice);
            expectedPrice += 1000;
        }
    }

}
