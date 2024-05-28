package com.bbangle.bbangle.board.repository.folder.cursor;

import static com.bbangle.bbangle.board.domain.QBoard.board;
import static com.bbangle.bbangle.wishlist.domain.QWishListBoard.wishListBoard;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.dto.BoardResponseDto;
import com.bbangle.bbangle.common.sort.FolderBoardSortType;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.RankingFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.page.BoardCustomPage;
import com.bbangle.bbangle.ranking.domain.Ranking;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import com.querydsl.core.BooleanBuilder;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WishListRecentCursorGeneratorTest extends AbstractIntegrationTest {

    private static final Long DEFAULT_CURSOR_ID = null;
    private static final Long DEFAULT_FOLDER_ID = 0L;

    Member member;
    WishListFolder wishListFolder;
    Long lastSavedId;
    Long firstSavedId;

    @BeforeEach
    void setup() {
        member = MemberFixture.createKakaoMember();
        member = memberService.getFirstJoinedMember(member);
        Member member2 = MemberFixture.createKakaoMember();
        member2 = memberService.getFirstJoinedMember(member2);
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
            Ranking ranking = RankingFixture.newRanking(createdBoard);
            rankingRepository.save(ranking);
            wishListBoardService.wish(member.getId(), createdBoard.getId(),
                new WishListBoardRequest(wishListFolder.getId()));
            if(createdBoard.getId() % 2 == 1) {
                wishListBoardService.wish(member2.getId(), createdBoard.getId(),
                    new WishListBoardRequest(DEFAULT_FOLDER_ID));
            }
        }
    }

    @Test
    @DisplayName("커서가 없는 경우 가장 커서 관련 조건문 없이 BooleanBuilder를 반환한다.")
    void getBoardWithWishListRecentWithoutCursor() {
        //given
        WishListRecentCursorGenerator wishListCursor = new WishListRecentCursorGenerator(queryFactory, DEFAULT_CURSOR_ID,
            member.getId());

        //when
        BooleanBuilder wishListCursorCondition = wishListCursor.getCursor();

        //then
        assertThat(wishListCursorCondition.getValue()).isNull();
    }

    @Test
    @DisplayName("커서가 존재하는 경우 그 커서보다 늦거나 같게 찜한 게시글 목록 BooleanBuilder를 반환한다.")
    void getBoardWithWishListRecentWithCursor() {
        //given
        WishListRecentCursorGenerator wishListRecentCursor = new WishListRecentCursorGenerator(queryFactory, lastSavedId,
            wishListFolder.getId());

        //when
        BooleanBuilder wishListRecentCursorCursorCondition = wishListRecentCursor.getCursor();
        WishListBoard lastWishListBoard = wishListBoardRepository.findByBoardIdAndMemberId(lastSavedId, member.getId()).get();
        String expectedCursorCondition = new BooleanBuilder().and(wishListBoard.id.loe(lastWishListBoard.getId())).toString();

        //then
        assertThat(wishListRecentCursorCursorCondition.getValue()).hasToString(expectedCursorCondition);
    }

    @Test
    @DisplayName("위시리스트 폴더에 존재하지 않는 cursorId는 예외가 발생한다.")
    void getBoardWithWishListRecentWithoutAnyWishHistory() {
        //given
        Random random = new Random();
        long randomNum = random.nextLong(10000) + 1;
        WishListRecentCursorGenerator wishListRecentCursor = new WishListRecentCursorGenerator(queryFactory, lastSavedId + randomNum,
            member.getId());

        //when, then
        assertThatThrownBy(wishListRecentCursor::getCursor)
            .isInstanceOf(BbangleException.class)
            .hasMessage(BbangleErrorCode.WISHLIST_BOARD_NOT_FOUND.getMessage());

    }

}
