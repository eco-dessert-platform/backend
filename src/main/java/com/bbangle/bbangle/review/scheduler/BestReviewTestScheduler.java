package com.bbangle.bbangle.review.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BestReviewTestScheduler implements ReviewScheduler{
    private final BestReviewSelectionScheduler bestReviewSelectionScheduler;

    @Override
    public void updateBestReview() {
        bestReviewSelectionScheduler.getBestReviewIds();
    }
}
