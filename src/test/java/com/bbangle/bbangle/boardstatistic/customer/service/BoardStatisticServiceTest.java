package com.bbangle.bbangle.boardstatistic.customer.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Store;
import com.bbangle.bbangle.boardstatistic.customer.ranking.UpdateBoardStatistic;
import com.bbangle.bbangle.boardstatistic.customer.update.StatisticUpdate;
import com.bbangle.bbangle.boardstatistic.customer.update.UpdateType;
import com.bbangle.bbangle.boardstatistic.domain.BoardPreferenceStatistic;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.BoardPreferenceStatisticFixture;
import com.bbangle.bbangle.fixture.BoardStatisticFixture;
import com.bbangle.bbangle.fixture.ProductFixture;
import com.bbangle.bbangle.fixture.ReviewFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.review.domain.Review;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

class BoardStatisticServiceTest extends AbstractIntegrationTest {

    private static final String STATISTIC_UPDATE_LIST = "STATISTIC_UPDATE_LIST";

    @Autowired
    BoardStatisticService boardStatisticService;
    @Autowired
    UpdateBoardStatistic updateBoardStatistic;
    @Autowired
    @Qualifier("updateRedisTemplate")
    RedisTemplate<String, Object> updateTemplate;

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
        updateBoardStatistic.updateStatistic();

        //then
        BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(board.getId())
            .orElseThrow();
        assertThat(boardStatistic.getBoardViewCount()).isOne();
    }

    @Test
    @DisplayName("정상적으로 Board의 review 작성 시 reviewCount를 업데이트한다")
    void updateReviewWriteCount() {
        //given, when
        boardStatisticService.updateReview(board.getId());

        //then
        List<StatisticUpdate> list = new ArrayList<>();
        while (updateTemplate.opsForList().size(STATISTIC_UPDATE_LIST) > 0) {
            list.add((StatisticUpdate) updateTemplate.opsForList().leftPop(STATISTIC_UPDATE_LIST));
        }

        //then
        assertThat(list).hasSize(1);
        list.forEach(
            update -> {
                Assertions.assertThat(update.boardId()).isEqualTo(board.getId());
                Assertions.assertThat(update.updateType()).isEqualTo(UpdateType.REVIEW);
            }
        );
    }

    @Test
    @DisplayName("정상적으로 Board의 review 삭제 시 reviewCount를 업데이트한다")
    void updateReviewRemoveCount() {
        //given, when
        boardStatisticService.updateReview(board.getId());
        boardStatisticService.updateReview(board.getId());

        //then
        List<StatisticUpdate> list = new ArrayList<>();
        while (updateTemplate.opsForList().size(STATISTIC_UPDATE_LIST) > 0) {
            list.add((StatisticUpdate) updateTemplate.opsForList().leftPop(STATISTIC_UPDATE_LIST));
        }

        //then
        assertThat(list).hasSize(2);
        list.forEach(
            update -> {
                Assertions.assertThat(update.boardId()).isEqualTo(board.getId());
                Assertions.assertThat(update.updateType()).isEqualTo(UpdateType.REVIEW);
            }
        );
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

        List<StatisticUpdate> list = new ArrayList<>();
        while (updateTemplate.opsForList().size(STATISTIC_UPDATE_LIST) > 0) {
            list.add((StatisticUpdate) updateTemplate.opsForList().leftPop(STATISTIC_UPDATE_LIST));
        }

        //then
        assertThat(list).hasSize(6);
        list.forEach(
            update -> {
                Assertions.assertThat(update.boardId()).isEqualTo(board.getId());
                Assertions.assertThat(update.updateType()).isEqualTo(UpdateType.REVIEW);
            }
        );
    }

    @Test
    @DisplayName("정상적으로 Board를 찜할 시 wishCount를 업데이트한다")
    void updateWishCount() {
        //given, when
        boardStatisticService.updateWishCount(board.getId());

        //then
        List<StatisticUpdate> list = new ArrayList<>();
        while (updateTemplate.opsForList().size(STATISTIC_UPDATE_LIST) > 0) {
            list.add((StatisticUpdate) updateTemplate.opsForList().leftPop(STATISTIC_UPDATE_LIST));
        }

        //then
        assertThat(list).hasSize(1);
        list.forEach(
            update -> {
                Assertions.assertThat(update.boardId()).isEqualTo(board.getId());
                Assertions.assertThat(update.updateType()).isEqualTo(UpdateType.WISH_COUNT);
            }
        );
    }

    @Test
    @DisplayName("정상적으로 Board를 찜 취소 시 wishCount를 업데이트한다")
    void updateWishCancelCount() {
        //given, when
        boardStatisticService.updateWishCount(board.getId());
        boardStatisticService.updateWishCount(board.getId());

        //then
        List<StatisticUpdate> list = new ArrayList<>();
        while (updateTemplate.opsForList().size(STATISTIC_UPDATE_LIST) > 0) {
            list.add((StatisticUpdate) updateTemplate.opsForList().leftPop(STATISTIC_UPDATE_LIST));
        }

        //then
        assertThat(list).hasSize(2);
        list.forEach(
            update -> {
                Assertions.assertThat(update.boardId()).isEqualTo(board.getId());
                Assertions.assertThat(update.updateType()).isEqualTo(UpdateType.WISH_COUNT);
            }
        );
    }

    private BigDecimal getAvgScore(List<Review> reviews) {
        BigDecimal sum = reviews.stream()
            .map(Review::getRate)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(reviews.size()), 2, RoundingMode.HALF_UP);
    }

    @Nested
    class UpdateTest {

        Board board;
        Board board2;

        @BeforeEach
        void setup() {
            while (updateTemplate.opsForList().size(STATISTIC_UPDATE_LIST) > 0) {
                updateTemplate.opsForList().leftPop(STATISTIC_UPDATE_LIST);
            }
            Store store = storeRepository.save(StoreFixture.storeGenerator());
            board = boardRepository.save(BoardFixture.randomBoard(store));
            board2 = boardRepository.save(BoardFixture.randomBoard(store));
            ProductFixture.randomProduct(board);
            ProductFixture.randomProduct(board2);

            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board));
            boardStatisticRepository.save(BoardStatisticFixture.newBoardStatistic(board2));
            preferenceStatisticRepository.saveAll(
                BoardPreferenceStatisticFixture.createBasicPreferenceStatistic(board.getId()));
            preferenceStatisticRepository.saveAll(
                BoardPreferenceStatisticFixture.createBasicPreferenceStatistic(board2.getId()));
        }

        @Test
        @DisplayName("정상적으로 게시글이 업데이트 된다.")
        void updateSuccess() {
            //given
            BoardStatistic boardStatisticb = boardStatisticRepository.findByBoardId(board.getId())
                .orElseThrow();
            BoardStatistic boardStatistic2b = boardStatisticRepository.findByBoardId(board2.getId())
                .orElseThrow();
            boardStatisticService.updateViewCount(board.getId());
            boardStatisticService.updateViewCount(board.getId());
            boardStatisticService.updateViewCount(board2.getId());
            boardStatisticService.updateViewCount(board2.getId());
            boardStatisticService.updateViewCount(board2.getId());

            //when
            updateBoardStatistic.updateStatistic();
            BoardStatistic boardStatistic = boardStatisticRepository.findByBoardId(board.getId())
                .orElseThrow();
            BoardStatistic boardStatistic2 = boardStatisticRepository.findByBoardId(board2.getId())
                .orElseThrow();
            List<BoardPreferenceStatistic> boardPreference = preferenceStatisticRepository.findAllByBoardId(
                board.getId());
            List<BoardPreferenceStatistic> boardPreference2 = preferenceStatisticRepository.findAllByBoardId(
                board2.getId());

            //then
            Assertions.assertThat(boardStatistic.getBoardViewCount()).isEqualTo(2);
            Assertions.assertThat(boardStatistic2.getBoardViewCount()).isEqualTo(3);
            Assertions.assertThat(boardStatistic.getBasicScore()).isEqualTo(2);
            Assertions.assertThat(boardStatistic2.getBasicScore()).isEqualTo(3);
            boardPreference.forEach(
                preference -> {
                    Assertions.assertThat(preference.getBasicScore())
                        .isEqualTo(boardStatistic.getBasicScore());
                    Assertions.assertThat(preference.getPreferenceScore())
                        .isEqualTo(preference.getBasicScore() * preference.getPreferenceWeight());
                }
            );

            boardPreference2.forEach(
                preference -> {
                    Assertions.assertThat(preference.getBasicScore())
                        .isEqualTo(boardStatistic2.getBasicScore());
                    Assertions.assertThat(preference.getPreferenceScore())
                        .isEqualTo(preference.getBasicScore() * preference.getPreferenceWeight());
                }
            );
        }
    }

}
