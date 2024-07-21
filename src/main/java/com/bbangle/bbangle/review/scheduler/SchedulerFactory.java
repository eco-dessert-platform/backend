package com.bbangle.bbangle.review.scheduler;

import com.bbangle.bbangle.image.repository.ImageRepository;
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
public class SchedulerFactory {
    private final ReviewRepository reviewRepository;
    private final ImageRepository imageRepository;
    private static final Long MINIMUM_BEST_REVIEW_STANDARD = 5L;
    private static final Long TEMP_DOMAINID = -1L;



    @Scheduled(cron = "0 0 12 * * ?")//매일 낮 12시
    @Transactional
    public void action() {
        log.info("Scheduler started ........");
        List<Long> bestReviewIds = getBestReviewIds();
        log.info("start update process");
        reviewRepository.updateBestReview(bestReviewIds);
        log.info("finish update process");
        log.info("start deleting temp Image Process");
        deleteTempImage();
        log.info("finish deleting temp Image Process");
    }

    public void deleteTempImage() {
        imageRepository.deleteTempImages(TEMP_DOMAINID);
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
