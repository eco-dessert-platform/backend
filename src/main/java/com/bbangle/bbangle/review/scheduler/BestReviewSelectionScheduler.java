package com.bbangle.bbangle.review.scheduler;

import com.bbangle.bbangle.review.dto.LikeCountPerReviewIdDto;
import com.bbangle.bbangle.review.dto.ReviewCountPerBoardIdDto;
import com.bbangle.bbangle.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BestReviewSelectionScheduler {
    private final ReviewRepository reviewRepository;
    private static final Long MINIMUM_BEST_REVIEW_STANDARD = 5L;


    @Scheduled(cron = "0 0 12 * * ?")//매일 낮 12시
    @Transactional
    public void updateBestReview() {
        log.info("Scheduler started ........");
        List<Long> bestReviewIds = getBestReviewIds();
        log.info("start update process");
        reviewRepository.updateBestReview(bestReviewIds);
        log.info("finish update process");
    }

    public List<Long> getBestReviewIds() {
        List<ReviewCountPerBoardIdDto> reviewCounts = reviewRepository.getReviewCount();
        List<LikeCountPerReviewIdDto> likeCounts = reviewRepository.getLikeCount(MINIMUM_BEST_REVIEW_STANDARD);
        Map<Long, Integer> bestReviewSizeMap = getBestReviewCount(reviewCounts);
        List<Long> candidateReviews = getCandidateReviews(likeCounts);
        Map<Long, List<Long>> selectedReviewPerBoardIdMap = reviewRepository.getBestReview(candidateReviews);
        selectedReviewPerBoardIdMap.forEach(
                (key, value) -> {
                    List<Long> newValue = value.stream()
                            .limit(bestReviewSizeMap.get(key))
                            .toList();
                    selectedReviewPerBoardIdMap.put(key, newValue);
                }
        );
        List<Long> bestReviewIds= new ArrayList<>();
        selectedReviewPerBoardIdMap.forEach((key, value) ->
                bestReviewIds.addAll(value)
            );
        return bestReviewIds;
    }

    private Map<Long, Integer> getBestReviewCount(List<ReviewCountPerBoardIdDto> reviewCounts) {
        Map<Long, Integer> bestReviewCountPerBoardId = new HashMap<>();
        for(ReviewCountPerBoardIdDto reviewCount : reviewCounts){
            Long boardId = reviewCount.boardId();
            Long count = reviewCount.reviewCount();
            Integer bestReviewSize = (int) Math.ceil((double) count * 0.05);
            bestReviewCountPerBoardId.put(boardId, bestReviewSize);
        }
        return bestReviewCountPerBoardId;
    }

    private List<Long> getCandidateReviews(List<LikeCountPerReviewIdDto> likeCounts){
        return likeCounts.stream()
                .map(
                        LikeCountPerReviewIdDto::reviewId
                )
                .toList();
    }
}
