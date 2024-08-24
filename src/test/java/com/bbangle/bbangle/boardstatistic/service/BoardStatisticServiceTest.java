package com.bbangle.bbangle.boardstatistic.service;

import static org.assertj.core.api.Assertions.*;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.boardstatistic.ranking.UpdateBoardStatistic;
import com.bbangle.bbangle.boardstatistic.repository.BoardPreferenceStatisticRepository;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardPreferenceStatisticFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.ReviewFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.store.domain.Store;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

class BoardStatisticServiceTest extends AbstractIntegrationTest {

    @Autowired
    BoardStatisticService boardStatisticService;
    @Autowired
    UpdateBoardStatistic updateBoardStatistic;

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
        List<Long> count = new ArrayList<>();
        for (Review value : reviewList) {
            boardStatisticService.updateReview(
                board.getId());
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
        boardStatisticService.updateReview(board.getId());
        boardStatisticService.updateReview(board.getId());

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
        boardStatisticService.updateReview(board.getId());
        boardStatisticService.updateReview(board.getId());
        boardStatisticService.updateReview(board.getId());
        boardStatisticService.updateReview(board.getId());
        boardStatisticService.updateReview(board.getId());
        boardStatisticService.updateReview(board.getId());

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
        boardStatisticService.updateWishCount(board.getId());

        //then
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(board.getId())
            .orElseThrow();
        assertThat(boardStatistic.getBoardWishCount()).isOne();
    }

    @Test
    @DisplayName("정상적으로 Board를 찜 취소 시 wishCount를 업데이트한다")
    void updateWishCancelCount() {
        //given, when
        boardStatisticService.updateWishCount(board.getId());
        boardStatisticService.updateWishCount(board.getId());

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

    @Nested
    class UpdateTest{

        Board board;
        Board board2;

        @BeforeEach
        void setup(){
            Store store = storeRepository.save(StoreFixture.storeGenerator());
            board = boardRepository.save(BoardFixture.randomBoard(store));
            board2 = boardRepository.save(BoardFixture.randomBoard(store));
            ProductFixture.randomProduct(board);
            ProductFixture.randomProduct(board2);

            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board));
            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board2));
            preferenceStatisticRepository.saveAll(BoardPreferenceStatisticFixture.createBasicPreferenceStatistic(board.getId()));
            preferenceStatisticRepository.saveAll(BoardPreferenceStatisticFixture.createBasicPreferenceStatistic(board2.getId()));
        }

        @Test
        @DisplayName("정상적으로 게시글이 업데이트 된다.")
        void updateSuccess() {
            //given
            boardStatisticService.updateViewCount(board.getId());
            boardStatisticService.updateViewCount(board.getId());
            boardStatisticService.updateViewCount(board2.getId());
            boardStatisticService.updateViewCount(board2.getId());
            boardStatisticService.updateViewCount(board2.getId());

            //when
            updateBoardStatistic.updateStatistic();
            BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(board.getId()).orElseThrow();
            BoardStatistic boardStatistic2 = boardStatisticRepository.findByBoardId(board2.getId()).orElseThrow();
            List<BoardPreferenceStatistic> boardPreference = preferenceStatisticRepository.findAllByBoardId(
                board.getId());
            List<BoardPreferenceStatistic> boardPreference2 = preferenceStatisticRepository.findAllByBoardId(board2.getId());

            //then
            Assertions.assertThat(boardStatistic.getBoardViewCount()).isEqualTo(2);
            Assertions.assertThat(boardStatistic2.getBoardViewCount()).isEqualTo(3);
            Assertions.assertThat(boardStatistic.getBasicScore()).isEqualTo(3);
            Assertions.assertThat(boardStatistic2.getBasicScore()).isEqualTo(3);
            boardPreference.forEach(
                preference -> {
                    Assertions.assertThat(preference.getBasicScore()).isEqualTo(boardStatistic.getBasicScore());
                    Assertions.assertThat(preference.getPreferenceScore()).isEqualTo(preference.getBasicScore() * preference.getPreferenceWeight());
                }
            );

            boardPreference2.forEach(
                preference -> {
                    Assertions.assertThat(preference.getBasicScore()).isEqualTo(boardStatistic2.getBasicScore());
                    Assertions.assertThat(preference.getPreferenceScore()).isEqualTo(preference.getBasicScore() * preference.getPreferenceWeight());
                }
            );
        }
    }

}
