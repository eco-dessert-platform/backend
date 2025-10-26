package com.bbangle.bbangle.config.ranking;

import static org.assertj.core.api.Assertions.assertThat;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.board.domain.Store;
import com.bbangle.bbangle.boardstatistic.customer.ranking.BoardStatisticConfig;
import com.bbangle.bbangle.boardstatistic.domain.BoardStatistic;
import com.bbangle.bbangle.fixture.BoardFixture;
import com.bbangle.bbangle.fixture.ReviewFixture;
import com.bbangle.bbangle.fixture.StoreFixture;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.wishlist.domain.WishListBoard;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BoardStatisticConfigTest extends AbstractIntegrationTest {

    @Autowired
    BoardStatisticConfig boardStatisticConfig;

    @Test
    @DisplayName("서버 가동 시 boardstatistic 정보 업데이트해주는 테스트")
    void statisticUpdateWhenInit() {
        //given
        Store store = storeRepository.save(StoreFixture.storeGenerator());
        Board board = boardRepository.save(BoardFixture.randomBoard(store));

        List<Review> reviewList = getReviewList(board);
        reviewRepository.saveAll(reviewList);
        List<WishListBoard> wishListBoardList = getWishListBoardList(board);
        wishListBoardRepository.saveAll(wishListBoardList);
        BigDecimal avgScore = getAvgScore(reviewList);

        //when
        boardStatisticConfig.init();
        BoardStatistic statistic = boardStatisticRepository.findByBoardId(board.getId())
            .orElseThrow();
        double expectedScore = getBasicScore(statistic);

        //then
        assertThat(statistic.getBoardReviewCount()).isEqualTo(reviewList.size());
        assertThat(statistic.getBoardWishCount()).isEqualTo(wishListBoardList.size());
        assertThat(statistic.getBoardReviewGrade().doubleValue()).isEqualTo(avgScore.doubleValue());
        assertThat(statistic.getBasicScore()).isEqualTo(expectedScore);
    }

    private List<Review> getReviewList(Board board) {
        return List.of(ReviewFixture.createReviewWithBoardIdAndRate(board.getId(), 4.0),
            ReviewFixture.createReviewWithBoardIdAndRate(board.getId(), 2.0),
            ReviewFixture.createReviewWithBoardIdAndRate(board.getId(), 1.0),
            ReviewFixture.createReviewWithBoardIdAndRate(board.getId(), 3.0));
    }

    private List<WishListBoard> getWishListBoardList(Board board) {
        return List.of(WishListBoard.builder()
                .boardId(board.getId())
                .build(),
            WishListBoard.builder()
                .boardId(board.getId())
                .build(),
            WishListBoard.builder()
                .boardId(board.getId())
                .build());
    }

    private BigDecimal getAvgScore(List<Review> reviews) {
        BigDecimal sum = reviews.stream()
            .map(Review::getRate)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(reviews.size()), 2, RoundingMode.HALF_UP);
    }

    private double getBasicScore(BoardStatistic statistic) {
        return statistic.getBoardWishCount() * 50
            + statistic.getBoardReviewCount() * 50
            + statistic.getBoardViewCount();
    }


}
