package com.bbangle.bbangle.board.repository.folder.cursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.RankingFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.ranking.domain.BoardStatistic;
import com.bbangle.bbangle.ranking.domain.QRanking;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.QWishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import com.querydsl.core.BooleanBuilder;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularCursorGeneratorTest extends AbstractIntegrationTest {

    private static final Long DEFAULT_CURSOR_ID = null;

    Member member;
    WishListFolder wishListFolder;
    Long lastSavedId;
    Long firstSavedId;

    @BeforeEach
    void setup() {
        member = MemberFixture.createKakaoMember();
        member = memberService.getFirstJoinedMember(member);
        Store store = StoreFixture.storeGenerator();
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
            if(i == 11){
                lastSavedId = createdBoard.getId();
            }
            Product product = ProductFixture.randomProduct(createdBoard);
            productRepository.save(product);
            BoardStatistic boardStatistic = RankingFixture.newRanking(createdBoard);
            boardStatisticRepository.save(boardStatistic);
            wishListBoardService.wish(member.getId(), createdBoard.getId(),
                new WishListBoardRequest(wishListFolder.getId()));
        }
    }


    @Test
    @DisplayName("커서가 없는 경우 가장 커서 관련 조건문 없이 BooleanBuilder를 반환한다.")
    void getBoardWithWishListRecentWithoutCursor() {
        //given
        PopularCursorGenerator popularCursorGenerator = new PopularCursorGenerator(queryFactory, DEFAULT_CURSOR_ID,
            member.getId());

        //when
        BooleanBuilder popularCursor = popularCursorGenerator.getCursor();

        //then
        assertThat(popularCursor.getValue()).isNull();
    }

    @Test
    @DisplayName("커서가 존재하는 경우 그 커서보다 인기점수가 낮거나 같은 게시글 목록 BooleanBuilder를 반환한다.")
    void getBoardWithWishListRecentWithCursor() {
        //given
        PopularCursorGenerator popularCursorGenerator = new PopularCursorGenerator(queryFactory, lastSavedId,
            wishListFolder.getId());

        //when
        BooleanBuilder popularCursor = popularCursorGenerator.getCursor();
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(lastSavedId).get();
        WishListBoard wish = wishListBoardRepository.findByBoardIdAndMemberId(lastSavedId, member.getId()).get();
        String expectedCursorCondition = new BooleanBuilder().and(QRanking.ranking.popularScore.loe(
            boardStatistic.getBasicScore()).and(
            QWishListBoard.wishListBoard.id.loe(wish.getId()))).toString();

        //then
        assertThat(popularCursor.getValue()).hasToString(expectedCursorCondition);
    }

    @Test
    @DisplayName("위시리스트 폴더에 존재하지 않는 cursorId는 예외가 발생한다.")
    void getBoardWithWishListRecentWithoutAnyWishHistory() {
        //given
        Random random = new Random();
        long randomNum = random.nextLong(10000) + 1;
        PopularCursorGenerator popularCursorGenerator = new PopularCursorGenerator(queryFactory, lastSavedId + randomNum,
            member.getId());

        //when, then
        assertThatThrownBy(popularCursorGenerator::getCursor)
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.WISHLIST_BOARD_NOT_FOUND.getMessage());

    }

}
