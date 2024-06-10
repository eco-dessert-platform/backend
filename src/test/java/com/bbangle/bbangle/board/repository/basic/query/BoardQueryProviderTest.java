package com.bbangle.bbangle.board.repository.basic.query;

import static org.assertj.core.api.Assertions.*;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.dao.BoardResponseDao;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.repository.basic.cursor.BoardCursorGeneratorMapping;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.RankingFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.ranking.domain.QRanking;
import com.bbangle.bbangle.ranking.domain.Ranking;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BoardQueryProviderTest extends AbstractIntegrationTest {

    private static final Long DEFAULT_FOLDER_ID = 0L;
    private static final Long NULL_CURSOR = null;
    private static final BooleanBuilder NON_FILTER_REQUEST = null;
    private static final int BOARD_SIZE = 12;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final Double ONE_WISH_SCORE = 50.0;

    @Autowired
    BoardCursorGeneratorMapping boardCursorGeneratorMapping;

    @Autowired
    BoardQueryProviderResolver boardQueryProviderResolver;

    private Store store;
    private Long lastSavedBoardId;
    private Long firstSavedBoardId;
    private Member member;

    @BeforeEach
    void setup() {
        Store newStore = StoreFixture.storeGenerator();
        store = storeRepository.save(newStore);
        for (int i = 0; i < BOARD_SIZE; i++) {
            member = MemberFixture.createKakaoMember();
            member = memberService.getFirstJoinedMember(member);
            Board newBoard = BoardFixture.randomBoardWithPrice(store, i * 1000 + 1000);
            Board savedBoard = boardRepository.save(newBoard);
            Ranking ranking = RankingFixture.newRanking(savedBoard);
            rankingRepository.save(ranking);
            if (i == 0) {
                firstSavedBoardId = savedBoard.getId();
            }

            if (i == 11) {
                lastSavedBoardId = savedBoard.getId();
            }

            Product firstProduct = ProductFixture.randomProduct(savedBoard);
            Product secondProduct = ProductFixture.randomProduct(savedBoard);
            productRepository.save(firstProduct);
            productRepository.save(secondProduct);
            if (i % 2 == 1) {
                wishListBoardService.wish(member.getId(), savedBoard.getId(),
                    new WishListBoardRequest(DEFAULT_FOLDER_ID));
            }
        }
    }

    @Nested
    @DisplayName("높은 가격순 쿼리")
    class HighPriceBoardQueryProviderTest {

        @Test
        @DisplayName("cursorId가 없는 경우에도 정상적으로 쿼리를 반환한다.")
        void getQueryWithOutCursorId() {
            //given
            BooleanBuilder highPriceCursor = boardCursorGeneratorMapping.mappingCursorGenerator(
                    SortType.HIGH_PRICE)
                .getCursor(NULL_CURSOR);
            OrderSpecifier<?>[] orderExpression = SortType.HIGH_PRICE.getOrderExpression();

            //when
            List<BoardResponseDao> actualBoards = boardQueryProviderResolver.resolve(
                    SortType.HIGH_PRICE)
                .findBoards(NON_FILTER_REQUEST, highPriceCursor, orderExpression);
            int beforePrice = actualBoards.stream()
                .findFirst()
                .map(BoardResponseDao::price)
                .orElseThrow();
            Long beforeBoardId = actualBoards.stream()
                .findFirst()
                .map(BoardResponseDao::boardId)
                .orElseThrow();

            //then
            for (int i = 1; i < actualBoards.size(); i++) {
                if (beforeBoardId.equals(actualBoards.get(i)
                    .boardId())) {
                    continue;
                }
                assertThat(actualBoards.get(i)
                    .price()).isLessThan(beforePrice);
                beforeBoardId = actualBoards.get(i)
                    .boardId();
                beforePrice = actualBoards.get(i)
                    .price();
            }
        }

        @Test
        @DisplayName("cursorId가 있는 경우에도 정상적으로 쿼리를 반환한다.")
        void getQueryWithCursorId() {
            //given
            BooleanBuilder highPriceCursor = boardCursorGeneratorMapping.mappingCursorGenerator(
                    SortType.HIGH_PRICE)
                .getCursor(lastSavedBoardId);
            OrderSpecifier<?>[] orderExpression = SortType.HIGH_PRICE.getOrderExpression();

            //when
            List<BoardResponseDao> actualBoards = boardQueryProviderResolver.resolve(
                    SortType.HIGH_PRICE)
                .findBoards(NON_FILTER_REQUEST, highPriceCursor, orderExpression);
            int beforePrice = actualBoards.stream()
                .findFirst()
                .map(BoardResponseDao::price)
                .orElseThrow();
            Long beforeBoardId = actualBoards.stream()
                .findFirst()
                .map(BoardResponseDao::boardId)
                .orElseThrow();

            //then
            assertThat(actualBoards).hasSize((DEFAULT_PAGE_SIZE + 1) * 2);
            for (int i = 1; i < actualBoards.size(); i++) {
                if (beforeBoardId.equals(actualBoards.get(i)
                    .boardId())) {
                    continue;
                }
                assertThat(actualBoards.get(i)
                    .price()).isLessThan(beforePrice);
                beforeBoardId = actualBoards.get(i)
                    .boardId();
                beforePrice = actualBoards.get(i)
                    .price();
            }
        }

    }

    @Nested
    @DisplayName("낮은 가격순 쿼리")
    class LowPriceBoardQueryProviderTest {

        @Test
        @DisplayName("cursorId가 없는 경우에도 정상적으로 쿼리를 반환한다.")
        void getQueryWithOutCursorId() {
            //given
            BooleanBuilder lowPriceCursor = boardCursorGeneratorMapping.mappingCursorGenerator(
                    SortType.LOW_PRICE)
                .getCursor(NULL_CURSOR);
            OrderSpecifier<?>[] orderExpression = SortType.LOW_PRICE.getOrderExpression();

            //when
            List<BoardResponseDao> actualBoards = boardQueryProviderResolver.resolve(
                    SortType.LOW_PRICE)
                .findBoards(NON_FILTER_REQUEST, lowPriceCursor, orderExpression);
            int beforePrice = actualBoards.stream()
                .findFirst()
                .map(BoardResponseDao::price)
                .orElseThrow();
            Long beforeBoardId = actualBoards.stream()
                .findFirst()
                .map(BoardResponseDao::boardId)
                .orElseThrow();

            //then
            for (int i = 1; i < actualBoards.size(); i++) {
                if (beforeBoardId.equals(actualBoards.get(i)
                    .boardId())) {
                    continue;
                }
                assertThat(actualBoards.get(i)
                    .price()).isGreaterThan(beforePrice);
                beforeBoardId = actualBoards.get(i)
                    .boardId();
                beforePrice = actualBoards.get(i)
                    .price();
            }
        }

        @Test
        @DisplayName("cursorId가 있는 경우에도 정상적으로 쿼리를 반환한다.")
        void getQueryWithCursorId() {
            //given
            BooleanBuilder lowPriceCursor = boardCursorGeneratorMapping.mappingCursorGenerator(
                    SortType.LOW_PRICE)
                .getCursor(firstSavedBoardId);
            OrderSpecifier<?>[] orderExpression = SortType.LOW_PRICE.getOrderExpression();

            //when
            List<BoardResponseDao> actualBoards = boardQueryProviderResolver.resolve(
                    SortType.LOW_PRICE)
                .findBoards(NON_FILTER_REQUEST, lowPriceCursor, orderExpression);
            int beforePrice = actualBoards.stream()
                .findFirst()
                .map(BoardResponseDao::price)
                .orElseThrow();
            Long beforeBoardId = actualBoards.stream()
                .findFirst()
                .map(BoardResponseDao::boardId)
                .orElseThrow();

            //then
            assertThat(actualBoards).hasSize((DEFAULT_PAGE_SIZE + 1) * 2);
            for (int i = 1; i < actualBoards.size(); i++) {
                if (beforeBoardId.equals(actualBoards.get(i)
                    .boardId())) {
                    continue;
                }
                assertThat(actualBoards.get(i)
                    .price()).isGreaterThan(beforePrice);
                beforeBoardId = actualBoards.get(i)
                    .boardId();
                beforePrice = actualBoards.get(i)
                    .price();
            }
        }

    }

    @Nested
    @DisplayName("최신 등록순 쿼리")
    class RecentBoardQueryProviderTest {

        @Test
        @DisplayName("cursorId가 없는 경우에도 정상적으로 쿼리를 반환한다.")
        void getQueryWithOutCursorId() {
            //given
            BooleanBuilder recentCursor = boardCursorGeneratorMapping.mappingCursorGenerator(
                    SortType.RECENT)
                .getCursor(NULL_CURSOR);
            OrderSpecifier<?>[] orderExpression = SortType.RECENT.getOrderExpression();

            //when
            List<BoardResponseDao> actualBoards = boardQueryProviderResolver.resolve(
                    SortType.RECENT)
                .findBoards(NON_FILTER_REQUEST, recentCursor, orderExpression);
            Long beforeBoardId = actualBoards.stream()
                .findFirst()
                .map(BoardResponseDao::boardId)
                .orElseThrow();

            //then
            for (int i = 1; i < actualBoards.size(); i++) {
                if (beforeBoardId.equals(actualBoards.get(i)
                    .boardId())) {
                    continue;
                }
                assertThat(actualBoards.get(i)
                    .boardId()).isLessThan(beforeBoardId);
                beforeBoardId = actualBoards.get(i)
                    .boardId();
            }
        }

        @Test
        @DisplayName("cursorId가 있는 경우에도 정상적으로 쿼리를 반환한다.")
        void getQueryWithCursorId() {
            //given
            BooleanBuilder recentCursor = boardCursorGeneratorMapping.mappingCursorGenerator(
                    SortType.RECENT)
                .getCursor(firstSavedBoardId);
            OrderSpecifier<?>[] orderExpression = SortType.RECENT.getOrderExpression();

            //when
            List<BoardResponseDao> actualBoards = boardQueryProviderResolver.resolve(
                    SortType.RECENT)
                .findBoards(NON_FILTER_REQUEST, recentCursor, orderExpression);

            //then
            assertThat(actualBoards).hasSize(2);
        }

    }

    @Nested
    @DisplayName("추천순 쿼리")
    class RecommendBoardQueryProviderTest {

        @Test
        @DisplayName("cursorId가 없는 경우에도 정상적으로 쿼리를 반환한다.")
        void getQueryWithOutCursorId() {
            //given
            BooleanBuilder recommendCursor = boardCursorGeneratorMapping.mappingCursorGenerator(
                    SortType.RECOMMEND)
                .getCursor(NULL_CURSOR);
            OrderSpecifier<?>[] orderExpression = SortType.RECOMMEND.getOrderExpression();

            //when
            List<BoardResponseDao> actualBoards = boardQueryProviderResolver.resolve(
                    SortType.RECOMMEND)
                .findBoards(NON_FILTER_REQUEST, recommendCursor, orderExpression);
            Long beforeBoardId = actualBoards.stream()
                .findFirst()
                .map(BoardResponseDao::boardId)
                .orElseThrow();
            List<Ranking> ranking = queryFactory.select(QRanking.ranking)
                .from(QRanking.ranking)
                .join(QBoard.board)
                .on(QRanking.ranking.board.eq(QBoard.board))
                .fetchJoin()
                .fetch();

            //then
            for (int i = 1; i < actualBoards.size() / 2; i++) {
                if (beforeBoardId.equals(actualBoards.get(i)
                    .boardId())) {
                    continue;
                }
                Long boardId = actualBoards.get(i).boardId();
                Double boardScore = ranking.stream()
                    .filter(rank -> rank.getBoard()
                        .getId()
                        .equals(boardId))
                    .map(Ranking::getRecommendScore)
                    .findFirst()
                    .orElseThrow();
                assertThat(boardScore).isEqualTo(ONE_WISH_SCORE);
                assertThat(boardId).isLessThan(beforeBoardId);
                beforeBoardId = actualBoards.get(i).boardId();
            }
        }

        @Test
        @DisplayName("cursorId가 있는 경우에도 정상적으로 쿼리를 반환한다.")
        void getQueryWithCursorId() {
            //given
            BooleanBuilder recommendCursor = boardCursorGeneratorMapping.mappingCursorGenerator(
                    SortType.RECOMMEND)
                .getCursor(lastSavedBoardId);
            OrderSpecifier<?>[] orderExpression = SortType.RECOMMEND.getOrderExpression();

            //when
            List<BoardResponseDao> actualBoards = boardQueryProviderResolver.resolve(
                    SortType.RECOMMEND)
                .findBoards(NON_FILTER_REQUEST, recommendCursor, orderExpression);

            //then
            assertThat(actualBoards).hasSize((DEFAULT_PAGE_SIZE + 1) * 2);
        }

    }

}
