package com.bbangle.bbangle.review;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.fixture.ReviewFixture;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.domain.ReviewLike;
import com.bbangle.bbangle.review.repository.ReviewLikeRepository;
import com.bbangle.bbangle.review.scheduler.BestReviewSelectionScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
 class BestReviewSchedulerTest extends AbstractIntegrationTest {
    @Autowired
    private BestReviewSelectionScheduler bestReviewSelectionScheduler;
    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @BeforeEach
    void setUp(){
        reviewLikeRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("도움되요가 5개 이상인 베스트 리뷰 선정에 성공한다")
    void getBestReview(){
        //given
        Long bestReviewId = 0L;
        for(int LikeCount = 5; LikeCount > 0; LikeCount--){
            bestReviewId = LikeCount == 5 ? getReviewIdIncludedLikeCount(LikeCount) : bestReviewId;
        }

        //when
        List<Long> bestReviewIds = bestReviewSelectionScheduler.getBestReviewIds();

        //Then
        assertThat(bestReviewIds)
                .hasSize(1)
                .contains(bestReviewId);
    }

    @Test
    @DisplayName("도움되요가 5개 이상인 리뷰가 없어 베스트 리뷰를 선정할 수 없다")
    void getNoBestReview(){
        //given
        for(int LikeCount = 4; LikeCount > 0; LikeCount--){
           getReviewIdIncludedLikeCount(LikeCount);
        }

        //when
        List<Long> bestReviewIds = bestReviewSelectionScheduler.getBestReviewIds();


        //Then
        assertThat(bestReviewIds)
                .isEmpty();
    }

    private Long getReviewIdIncludedLikeCount(int LikeCount){
        Long boardId = 1L;
        Long memberId = 0L;
        Review review = ReviewFixture.createReviewWithBoardIdAndRate(boardId, 4.0);
        reviewRepository.save(review);
        for(int i = 0; i < LikeCount; i++){
            ReviewLike reviewLike = ReviewFixture.createReviewLike(review, memberId++);
            reviewLikeRepository.save(reviewLike);
        }
        return review.getId();
    }
}
