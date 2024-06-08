package com.bbangle.bbangle.board.repository;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForClassTypes.in;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.config.ranking.BoardWishListConfig;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.ranking.domain.Ranking;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.store.dto.BoardsInStoreDto;
import com.bbangle.bbangle.store.dto.PopularBoardDto;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BoardRepositoryTest extends AbstractIntegrationTest {

    private static final String TEST_TITLE = "TestTitle";
    private static final Long NULL_MEMBER_ID = null;

    @Autowired
    private BoardWishListConfig boardWishListConfig;

    @Test
    @DisplayName("getAllBoardTitle 정상 확인")
    void getAllBoardTitle() {
        Board board1 = fixtureBoard(emptyMap());
        Board board2 = fixtureBoard(emptyMap());

        HashMap<Long, String> result = boardRepository.getAllBoardTitle();

        // then
        assertThat(result).containsEntry(board1.getId(), board1.getTitle());
        assertThat(result).containsEntry(board2.getId(), board2.getTitle());
    }

    @Test
    @DisplayName("checkingNullRanking 정상 확인")
    void checkingNullRanking() {
        // given
        Board fixtureBoard = fixtureBoard(emptyMap());
        Board fixtureBoard2 = fixtureBoard(emptyMap());
        Ranking target = fixtureRanking(Map.of("board", fixtureBoard));
        Ranking nonTarget = fixtureRanking(Map.of("board", fixtureBoard2));

        nonTarget.setBoard(null);
        rankingRepository.save(nonTarget);

        // when
        List<Board> result = boardRepository.checkingNullRanking();

        assertThat(result).hasSize(1);
    }

    @Nested
    @DisplayName("getTopBoardIds 메서드는")
    class GetTopBoardIds {

        private Store store;
        private Ranking firstBoard;
        private Ranking secondBoard;
        private Ranking thirdBoard;

        @BeforeEach
        void init() {
            store = fixtureStore(emptyMap());

            secondBoard = fixtureRanking(
                Map.of("board", fixtureBoard(Map.of("store", store)), "popularScore", 101d)); // 2등
            firstBoard = fixtureRanking(
                Map.of("board", fixtureBoard(Map.of("store", store)), "popularScore", 102d)); // 1등
            thirdBoard = fixtureRanking(
                Map.of("board", fixtureBoard(Map.of("store", store)), "popularScore", 100d)); // 3등
        }

        @Test
        @DisplayName("인기순이 높은 스토어 게시글을 순서대로 가져올 수 있다")
        void getPopularBoard() {
            List<Long> rankBoards = List.of(
                firstBoard.getBoard().getId(),
                secondBoard.getBoard().getId(),
                thirdBoard.getBoard().getId());

            List<Long> rankBoardIds = boardRepository.getTopBoardIds(store.getId());

            assertThat(rankBoardIds).containsExactlyElementsOf(rankBoards);
        }
    }

    @Nested
    @DisplayName("getTopBoardInfo 메서드는")
    class GetTopBoardInfo {

        private Store store;
        private Member member;
        private List<Board> boards;
        private Board board;

        @BeforeEach
        void init() {
            store = fixtureStore(emptyMap());

            boards = List.of(
                fixtureBoard(Map.of("store", store, "title", TEST_TITLE)),
                fixtureBoard(Map.of("store", store, "title", TEST_TITLE)),
                fixtureBoard(Map.of("store", store, "title", TEST_TITLE))
            );

            createWishListStore();
        }

        @Test
        @DisplayName("인기순이 높은 스토어 게시글을 순서대로 가져올 수 있다")
        void getPopularBoard() {
            List<Long> boardIds = boards.stream().map(Board::getId).toList();
            List<PopularBoardDto> rankBoardIds = boardRepository.getTopBoardInfo(boardIds,
                NULL_MEMBER_ID);

            assertThat(rankBoardIds).hasSize(3);

            String title = rankBoardIds.stream().findFirst().get().getBoardTitle();
            assertThat(title).isEqualTo(TEST_TITLE);
        }

        @Test
        @DisplayName("위시리스트 등록한 상품을 정상적으로 가져올 가져올 수 있다")
        void getIsWished() {
            List<Long> boardIds = boards.stream().map(Board::getId).toList();
            List<PopularBoardDto> rankBoardIds = boardRepository.getTopBoardInfo(boardIds,
                member.getId());

            Boolean wishTrue = rankBoardIds.get(0)
                .getIsWished();

            Boolean wishFalse = rankBoardIds.get(1)
                .getIsWished();

            assertThat(wishTrue).isTrue();
            assertThat(wishFalse).isFalse();
        }

        void createWishListStore() {
            member = memberRepository.save(Member.builder().build());
            board = boards.stream().findFirst().get();
            wishListBoardRepository.save(WishListBoard.builder()
                .boardId(board.getId())
                .memberId(member.getId())
                .build());
        }
    }

    @Nested
    @DisplayName("getBoardIds 메서드는")
    class GetBoardIds {

        private final Long NULL_CURSOR = null;
        private Store store;

        @BeforeEach
        void init() {
            store = fixtureStore(emptyMap());

            for (int index = 0; 20 > index; index++) {
                fixtureBoard(Map.of("store", store, "title", TEST_TITLE));
            }
        }

        @Test
        @DisplayName("유효한 커서 아이디를 사용할 때, 제한된 게시글들을 가져올 수 있다")
        void getBoards() {
            List<Long> boardIdsByNullCursor = boardRepository.getBoardIds(NULL_CURSOR, store.getId());
            Long cursorId = boardIdsByNullCursor.get(boardIdsByNullCursor.size() -1);
            List<Long> boardIds = boardRepository.getBoardIds(cursorId, store.getId());

            assertThat(boardIdsByNullCursor).hasSize(11);
            assertThat(boardIds).hasSize(9);
        }
    }

    @Nested
    @DisplayName("findByBoardIds 메서드는")
    class FindByBoardIds {

        private final Long NULL_CURSOR = null;
        private Store store;
        private Board board1;
        private Board board2;
        private Board board3;
        private Board board4;

        @BeforeEach
        void init() {
            store = fixtureStore(emptyMap());

            board1 = fixtureBoard(Map.of("store", store, "title", TEST_TITLE));
            board2 = fixtureBoard(Map.of("store", store, "title", TEST_TITLE));
            board3 = fixtureBoard(Map.of("store", store, "title", TEST_TITLE));
            board4 = fixtureBoard(Map.of("store", store, "title", TEST_TITLE));
        }

        @Test
        @DisplayName("유효한 커서 아이디를 사용할 때, 제한된 게시글들을 가져올 수 있다")
        void getBoards() {
            List<Long> boardIds = List.of(
                board1.getId(),
                board2.getId(),
                board3.getId(),
                board4.getId());

            List<BoardsInStoreDto> boardsInStoreDtos = boardRepository.findByBoardIds(boardIds, NULL_MEMBER_ID);

            String title = boardsInStoreDtos.stream()
                .findFirst()
                .get()
                .getBoardTitle();

            assertThat(boardsInStoreDtos).hasSize(4);
            assertThat(title).isEqualTo(TEST_TITLE);
        }
    }
}