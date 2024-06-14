package com.bbangle.bbangle.board.repository;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.board.dto.BoardAllTitleDto;
import com.bbangle.bbangle.board.dto.BoardAndImageDto;
import com.bbangle.bbangle.store.dto.BoardsInStoreDto;
import com.bbangle.bbangle.store.dto.PopularBoardDto;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BoardRepositoryTest extends AbstractIntegrationTest {

    private final String TEST_TITLE = "TestTitle";

    private static final Long NULL_MEMBER_ID = null;

    @Nested
    @DisplayName("findBoardAndBoardImageByBoardId 메서드는")
    class FindBoardAndBoardImageByBoardId {

        private Board targetBoard;
        private String TEST_URL = "www.TESTURL.com";
        private final Long NOT_EXIST_BOARD_ID = -1L;

        @BeforeEach
        void init() {
            targetBoard = fixtureBoard(Map.of("title", TEST_TITLE));
            fixtureBoardImage(Map.of("board", targetBoard, "url", TEST_URL));
            fixtureBoardImage(Map.of("board", targetBoard, "url", TEST_URL));
        }

        @Test
        @DisplayName("board id가 유효할 때 게시판 정보, 게시판 이미지를 조회할 수 있다.")
        void getBoardInfoAndImages() {
            List<BoardAndImageDto> boardAndImageDtos = boardRepository
                .findBoardAndBoardImageByBoardId(targetBoard.getId());

            assertThat(boardAndImageDtos).hasSize(2);

            BoardAndImageDto boardAndImageDto = boardAndImageDtos.stream()
                .findFirst()
                .get();

            assertAll(
                "BoardAndImageDto는 null이 없어야한다.",
                () -> assertThat(boardAndImageDto.id()).isNotNull(),
                () -> assertThat(boardAndImageDto.url()).isNotNull()
            );
        }

        @Test
        @DisplayName("board id가 유효하지 않을 때, 빈 배열을 반환한다.")
        void getEmptyList() {
            List<BoardAndImageDto> boardAndImageDtos = boardRepository
                .findBoardAndBoardImageByBoardId(NOT_EXIST_BOARD_ID);

            assertThat(boardAndImageDtos).isEmpty();
        }
    }

    @Test
    @DisplayName("게시판 전체의 아이디와 게시글명을 가져올 수 있다.")
    void getAllBoardTitle() {
        fixtureBoard(Map.of("title", TEST_TITLE));
        fixtureBoard(Map.of("title", TEST_TITLE));
        List<BoardAllTitleDto> boardAllTitleDtos = boardRepository.findTitleByBoardAll();

        assertThat(boardAllTitleDtos).hasSize(2);
        boardAllTitleDtos.forEach(boardAllTitleDto -> {
            assertThat(boardAllTitleDto.boardId()).isNotNull();
            assertThat(boardAllTitleDto.Title()).isNotNull();
        });

    }

    @Test
    @DisplayName("checkingNullRanking 정상 확인")
    void checkingNullRanking() {
        // given
        Store store = StoreFixture.storeGenerator();
        storeRepository.save(store);
        Board fixtureBoard = BoardFixture.randomBoard(store);
        Board fixtureBoard2 = BoardFixture.randomBoard(store);
        List<Board> boards = List.of(fixtureBoard, fixtureBoard2);
        boardRepository.saveAll(boards);
        BoardStatistic nonTarget = BoardStatisticFixture.newBoardStatistic(fixtureBoard2);
        boardStatisticRepository.save(nonTarget);

        // when
        List<Board> result = boardRepository.checkingNullRanking();

        assertThat(result).hasSize(1);
    }

    @Nested
    @DisplayName("getTopBoardIds 메서드는")
    class GetTopBoardIds {

        private Store store;
        private Board firstBoard;
        private Board secondBoard;
        private Board thirdBoard;

        @BeforeEach
        void init() {
            store = fixtureStore(emptyMap());
            firstBoard = boardRepository.save(BoardFixture.randomBoard(store));
            secondBoard = boardRepository.save(BoardFixture.randomBoard(store));
            thirdBoard = boardRepository.save(BoardFixture.randomBoard(store));

            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatisticWithBasicScore(firstBoard, 103d));
            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatisticWithBasicScore(secondBoard, 102d));
            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatisticWithBasicScore(thirdBoard, 101d));
        }

        @Test
        @DisplayName("인기순이 높은 스토어 게시글을 순서대로 가져올 수 있다")
        void getPopularBoard() {
            List<Long> rankBoards = List.of(
                firstBoard.getId(),
                secondBoard.getId(),
                thirdBoard.getId()
                );
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

        private static final Long NULL_CURSOR = null;
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
            boardStatisticRepository.saveAll(
                List.of(BoardStatisticFixture.newBoardStatistic(board1),
                BoardStatisticFixture.newBoardStatistic(board2),
                BoardStatisticFixture.newBoardStatistic(board3),
                BoardStatisticFixture.newBoardStatistic(board4))
            );
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
