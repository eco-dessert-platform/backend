package com.bbangle.bbangle.board.repository.basic.cursor;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Product;
import com.bbangle.bbangle.board.domain.QBoard;
import com.bbangle.bbangle.board.sort.SortType;
import com.bbangle.bbangle.boardstatistic.domain.QBoardStatistic;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.MemberFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.member.domain.Member;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.store.domain.Store;
import com.bbangle.bbangle.wishlist.dto.WishListBoardRequest;
import com.querydsl.core.BooleanBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CursorGeneratorTest extends AbstractIntegrationTest {

    private static final Long DEFAULT_FOLDER_ID = 0L;
    private static final Long NULL_CURSOR = null;

    private Store store;
    private Long lastSavedBoardId;
    private Long firstSavedBoardId;
    private Member member;

    @Autowired
    BoardCursorGeneratorMapping boardCursorGeneratorMapping;

    @BeforeEach
    void setup() {
        Store newStore = StoreFixture.storeGenerator();
        store = storeRepository.save(newStore);
        for (int i = 0; i < 12; i++) {
            member = MemberFixture.createKakaoMember();
            member = memberService.getFirstJoinedMember(member);
            Board newBoard = BoardFixture.randomBoardWithPrice(store, i * 1000 + 1000);
            Board savedBoard = boardRepository.save(newBoard);
            BoardStatistic boardStatistic = BoardStatisticFixture.newBoardStatistic(savedBoard);
            boardStatisticRepository.save(boardStatistic);
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
    class HighPriceCursorGeneratorTest {

        @Test
        @DisplayName("cursorId가 없는 경우에도 정상적으로 높은 가격순의 cursor를 반환한다")
        void getHighPriceCursorWithoutCursorId() {
            //given, when
            BooleanBuilder HighPriceCursor = boardCursorGeneratorMapping
                .mappingCursorGenerator(SortType.HIGH_PRICE)
                .getCursor(NULL_CURSOR);

            //then
            assertThat(HighPriceCursor.getValue()).isNull();
        }

        @Test
        @DisplayName("존재하는 cursorId로 조회하는 경우 정상적으로 BooleanBuilder를 반환한다")
        void getHighPriceCursorWithCursorId() {
            //given, when
            BooleanBuilder highPriceCursor = boardCursorGeneratorMapping
                .mappingCursorGenerator(SortType.HIGH_PRICE)
                .getCursor(firstSavedBoardId);

            Integer price = queryFactory.select(QBoard.board.price)
                .from(QBoard.board)
                .where(QBoard.board.id.eq(firstSavedBoardId))
                .fetchOne();

            String expectedBuilder = new BooleanBuilder().and(QBoard.board.price.lt(price))
                .or(QBoard.board.price.eq(price).and(QBoard.board.id.loe(firstSavedBoardId)))
                .toString();

            //then
            assertThat(highPriceCursor.toString()).isEqualTo(expectedBuilder);
        }

        @Test
        @DisplayName("존재하지 않는 cursorId로 조회하는 경우 예외가 발생한다.")
        void getHighPriceCursorWithNotExistCursorId() {
            //given, when, then
            assertThatThrownBy(() -> boardCursorGeneratorMapping
                .mappingCursorGenerator(SortType.HIGH_PRICE)
                .getCursor(lastSavedBoardId + 1))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.BOARD_NOT_FOUND.getMessage());
        }

    }

    @Nested
    class LowPriceCursorGeneratorTest {

        @Test
        @DisplayName("cursorId가 없는 경우에도 정상적으로 높은 가격순의 cursor를 반환한다")
        void getLowPriceCursorWithoutCursorId() {
            //given, when
            BooleanBuilder lowPriceCursor = boardCursorGeneratorMapping
                .mappingCursorGenerator(SortType.LOW_PRICE)
                .getCursor(NULL_CURSOR);

            //then
            assertThat(lowPriceCursor.getValue()).isNull();
        }

        @Test
        @DisplayName("존재하는 cursorId로 조회하는 경우 정상적으로 BooleanBuilder를 반환한다")
        void getLowPriceCursorWithCursorId() {
            //given, when
            BooleanBuilder lowPriceCursor = boardCursorGeneratorMapping
                .mappingCursorGenerator(SortType.LOW_PRICE)
                .getCursor(firstSavedBoardId);

            Integer price = queryFactory.select(QBoard.board.price)
                .from(QBoard.board)
                .where(QBoard.board.id.eq(firstSavedBoardId))
                .fetchOne();

            String expectedBuilder = new BooleanBuilder().and(QBoard.board.price.gt(price))
                .or(QBoard.board.price.eq(price).and(QBoard.board.id.loe(firstSavedBoardId)))
                .toString();

            //then
            assertThat(lowPriceCursor.toString()).isEqualTo(expectedBuilder);
        }

        @Test
        @DisplayName("존재하지 않는 cursorId로 조회하는 경우 예외가 발생한다.")
        void getLowPriceCursorWithNotExistCursorId() {
            //given, when, then
            assertThatThrownBy(() -> boardCursorGeneratorMapping
                .mappingCursorGenerator(SortType.LOW_PRICE)
                .getCursor(lastSavedBoardId + 1))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.BOARD_NOT_FOUND.getMessage());
        }

    }

    @Nested
    class RecentCursorGeneratorTest {

        @Test
        @DisplayName("cursorId가 없는 경우에도 정상적으로 최신등록순 cursor를 반환한다")
        void getRecentCursorWithoutCursorId() {
            //given, when
            BooleanBuilder recentCursor = boardCursorGeneratorMapping
                .mappingCursorGenerator(SortType.RECENT)
                .getCursor(NULL_CURSOR);

            //then
            assertThat(recentCursor.getValue()).isNull();
        }

        @Test
        @DisplayName("존재하는 cursorId로 조회하는 경우 정상적으로 BooleanBuilder를 반환한다")
        void getRecentPriceCursorWithCursorId() {
            //given, when
            BooleanBuilder recentCursor = boardCursorGeneratorMapping
                .mappingCursorGenerator(SortType.RECENT)
                .getCursor(firstSavedBoardId);

            String expectedBuilder = new BooleanBuilder()
                .and(QBoard.board.id.loe(firstSavedBoardId))
                .toString();

            //then
            assertThat(recentCursor.toString()).isEqualTo(expectedBuilder);
        }

        @Test
        @DisplayName("존재하지 않는 cursorId로 조회하는 경우 예외가 발생한다.")
        void getLowPriceCursorWithNotExistCursorId() {
            //given, when, then
            assertThatThrownBy(() -> boardCursorGeneratorMapping
                .mappingCursorGenerator(SortType.RECENT)
                .getCursor(lastSavedBoardId + 1))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.BOARD_NOT_FOUND.getMessage());
        }

    }

    @Nested
    class RecommendCursorGenerator {

        @Test
        @DisplayName("cursorId가 없는 경우에도 정상적으로 추천순의 cursor를 반환한다")
        void getRecommendCursorWithoutCursorId() {
            //given, when
            BooleanBuilder recommendCursor = boardCursorGeneratorMapping
                .mappingCursorGenerator(SortType.RECOMMEND)
                .getCursor(NULL_CURSOR);

            //then
            assertThat(recommendCursor.getValue()).isNull();
        }

        @Test
        @DisplayName("존재하는 cursorId로 조회하는 경우 정상적으로 BooleanBuilder를 반환한다")
        void getLowPriceCursorWithCursorId() {
            //given, when
            BooleanBuilder recommendCursor = boardCursorGeneratorMapping
                .mappingCursorGenerator(SortType.RECOMMEND)
                .getCursor(firstSavedBoardId);

            Double expectedScore = queryFactory.select(QBoardStatistic.boardStatistic.basicScore)
                .from(QBoardStatistic.boardStatistic)
                .where(QBoardStatistic.boardStatistic.boardId.eq(firstSavedBoardId))
                .fetchOne();

            String expectedBuilder = new BooleanBuilder()
                .and(QBoardStatistic.boardStatistic.basicScore.lt(expectedScore))
                .or(QBoardStatistic.boardStatistic.basicScore.eq(expectedScore).and(QBoard.board.id.loe(firstSavedBoardId)))
                .toString();

            //then
            assertThat(recommendCursor.toString()).isEqualTo(expectedBuilder);
        }

        @Test
        @DisplayName("존재하지 않는 cursorId로 조회하는 경우 예외가 발생한다.")
        void getLowPriceCursorWithNotExistCursorId() {
            //given, when, then
            assertThatThrownBy(() -> boardCursorGeneratorMapping
                .mappingCursorGenerator(SortType.RECOMMEND)
                .getCursor(lastSavedBoardId + 1))
                .isInstanceOf(BbangleException.class)
                .hasMessage(BbangleErrorCode.RANKING_NOT_FOUND.getMessage());
        }

    }

}
