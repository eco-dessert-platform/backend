package com.bbangle.bbangle.boardstatistic.service;

import static org.assertj.core.api.Assertions.*;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.ReviewFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.store.domain.Store;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

class BoardStatisticServiceTest extends AbstractIntegrationTest {

    Logger logger = LoggerFactory.getLogger(BoardStatisticServiceTest.class);

    private static final Boolean IS_CREATE = true;
    private static final Boolean IS_REMOVE = false;

    @Autowired
    BoardStatisticService boardStatisticService;

    Board board;
    Review review;

    List<Review> reviewList = List.of(
        ReviewFixture.createReviewWithRate(BigDecimal.valueOf(4.0)),
        ReviewFixture.createReviewWithRate(BigDecimal.valueOf(5.0)),
        ReviewFixture.createReviewWithRate(BigDecimal.valueOf(2.0)),
        ReviewFixture.createReviewWithRate(BigDecimal.valueOf(1.0)),
        ReviewFixture.createReviewWithRate(BigDecimal.valueOf(1.0)),
        ReviewFixture.createReviewWithRate(BigDecimal.valueOf(1.0)),
        ReviewFixture.createReviewWithRate(BigDecimal.valueOf(1.0)),
        ReviewFixture.createReviewWithRate(BigDecimal.valueOf(3.0)),
        ReviewFixture.createReviewWithRate(BigDecimal.valueOf(4.0))
    );

    BigDecimal avgScore = getAvgScore(reviewList);

    @BeforeEach
    void setup() {
        Store store = storeRepository.save(StoreFixture.storeGenerator());
        board = boardRepository.save(BoardFixture.randomBoard(store));
        boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board));
        review = ReviewFixture.createReview();
    }

    @AfterEach
    void cleanUp() {
        boardRepository.deleteAll();
        boardStatisticRepository.deleteAll();
    }

    @Test
    @DisplayName("정상적으로 Board의 viewCount를 업데이트한다")
    void updateViewCount() {
        //given, when
        boardStatisticService.updateViewCount(board.getId());

        //then
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(board.getId())
            .orElseThrow();
        assertThat(boardStatistic.getBoardViewCount()).isOne();
    }

    @Test
    @DisplayName("정상적으로 Board의 review 작성 시 reviewCount를 업데이트한다")
    void updateReviewWriteCount() {
        //given, when

        ArrayList<BigDecimal> rates = new ArrayList<>();
        ArrayList<BigDecimal> boardReviewGrade = new ArrayList<>();
        List<Integer> count = new ArrayList<>();
        for (Review value : reviewList) {
            boardStatisticService.updateReviewCount(
                board.getId(),
                value.getRate(),
                IS_CREATE);
            BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(board.getId())
                .orElseThrow();
            rates.add(value.getRate());
            count.add(boardStatistic.getBoardReviewCount());
            boardReviewGrade.add(boardStatistic.getBoardReviewGrade());
        }

        //then
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(board.getId())
            .orElseThrow();
        assertThat(boardStatistic.getBoardReviewCount()).isEqualTo(reviewList.size());
        assertThat(boardStatistic.getBoardReviewGrade().doubleValue()).isEqualTo(avgScore.doubleValue());
    }

    @Test
    @DisplayName("정상적으로 Board의 review 삭제 시 reviewCount를 업데이트한다")
    void updateReviewRemoveCount() {
        //given, when
        boardStatisticService.updateReviewCount(board.getId(), review.getRate(), IS_CREATE);
        boardStatisticService.updateReviewCount(board.getId(), review.getRate(), IS_REMOVE);

        //then
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(board.getId())
            .orElseThrow();
        assertThat(boardStatistic.getBoardReviewCount()).isZero();
        assertThat(boardStatistic.getBoardReviewGrade()).isZero();
    }

    @Test
    @DisplayName("정상적으로 Board의 review 여러 개삭제 시 reviewCount를 업데이트한다")
    void updateReviewRemoveCountWithManySource() {
        //given, when
        boardStatisticService.updateReviewCount(board.getId(), reviewList.get(0).getRate(), IS_CREATE);
        boardStatisticService.updateReviewCount(board.getId(), reviewList.get(1).getRate(), IS_CREATE);
        boardStatisticService.updateReviewCount(board.getId(), reviewList.get(2).getRate(), IS_CREATE);
        boardStatisticService.updateReviewCount(board.getId(), reviewList.get(3).getRate(), IS_CREATE);
        boardStatisticService.updateReviewCount(board.getId(), reviewList.get(1).getRate(), IS_REMOVE);
        boardStatisticService.updateReviewCount(board.getId(), reviewList.get(2).getRate(), IS_REMOVE);

        BigDecimal expectedScore = getAvgScore(List.of(reviewList.get(0), reviewList.get(3)));
        //then
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(board.getId())
            .orElseThrow();
        assertThat(boardStatistic.getBoardReviewCount()).isEqualTo(2);
        assertThat(boardStatistic.getBoardReviewGrade().doubleValue()).isEqualTo(expectedScore.doubleValue());
    }

    @Test
    @DisplayName("정상적으로 Board를 찜할 시 wishCount를 업데이트한다")
    void updateWishCount() {
        //given, when
        boardStatisticService.updateWishCount(board.getId(), IS_CREATE);

        //then
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(board.getId())
            .orElseThrow();
        assertThat(boardStatistic.getBoardWishCount()).isOne();
    }

    @Test
    @DisplayName("정상적으로 Board를 찜 취소 시 wishCount를 업데이트한다")
    void updateWishCancelCount() {
        //given, when
        boardStatisticService.updateWishCount(board.getId(), IS_CREATE);
        boardStatisticService.updateWishCount(board.getId(), IS_REMOVE);

        //then
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(board.getId())
            .orElseThrow();
        assertThat(boardStatistic.getBoardWishCount()).isZero();
    }

    private BigDecimal getAvgScore(List<Review> reviews){
        BigDecimal sum = reviews.stream()
            .map(Review::getRate)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(reviews.size()), 2, RoundingMode.HALF_UP);
    }

}
