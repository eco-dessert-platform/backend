package com.bbangle.bbangle.review;

import com.bbangle.bbangle.AbstractIntegrationTest;
import com.bbangle.bbangle.fixture.ReviewFixture;
import com.bbangle.bbangle.review.domain.Review;
import com.bbangle.bbangle.review.domain.ReviewLike;
import com.bbangle.bbangle.review.repository.ReviewLikeRepository;
import com.bbangle.bbangle.review.scheduler.SchedulerFactory;
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
    private SchedulerFactory schedulerFactory;
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
        for(int likeCount = 5; likeCount > 0; likeCount--){
            bestReviewId = likeCount == 5 ? getReviewIdIncludedLikeCount(likeCount) : bestReviewId;
        }

        //when
        List<Long> bestReviewIdCandidates = schedulerFactory.getBestReviewIds();
        reviewRepository.updateBestReview(bestReviewIdCandidates);
        List<Long> bestReviewIds = reviewRepository.getBestReviewIds();
        //Then
        assertThat(bestReviewIds)
                .hasSize(1)
                .contains(bestReviewId);
    }

    @Test
    @DisplayName("도움되요가 5개 이상인 리뷰가 없어 베스트 리뷰를 선정할 수 없다")
    void getNoBestReview(){
        //given
        for(int likeCount = 4; likeCount > 0; likeCount--){
           getReviewIdIncludedLikeCount(likeCount);
        }

        //when
        List<Long> bestReviewIds = schedulerFactory.getBestReviewIds();


        //Then
        assertThat(bestReviewIds)
                .isEmpty();
    }

    private Long getReviewIdIncludedLikeCount(int likeCount){
        Long boardId = 1L;
        Long memberId = 0L;
        Review review = ReviewFixture.createReviewWithBoardIdAndRate(boardId, 4.0);
        reviewRepository.save(review);
        for(int i = 0; i < likeCount; i++){
            ReviewLike reviewLike = ReviewFixture.createReviewLike(review, memberId++);
            reviewLikeRepository.save(reviewLike);
        }
        return review.getId();
    }
}
