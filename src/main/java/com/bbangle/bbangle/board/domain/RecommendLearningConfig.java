package com.bbangle.bbangle.board.domain;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(timeToLive = 12 * 60 * 60) // 12시간
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RecommendLearningConfig {

    private static final int START_CURSOR_POINT = 0;
    private static final int LIMIT_REVIEW_DATA = 1000;

    @Id
    private RedisKeyEnum id;

    private Boolean isStoreUploadComplete;
    private Boolean isBoardUploadComplete;
    private Boolean isReviewUploadComplete;
    private Integer currentCursor;

    public RecommendLearningConfig( ) {
        this.id = RedisKeyEnum.RECOMMENDATION_LEARNING_CONFIG;
        this.isStoreUploadComplete = false;
        this.isBoardUploadComplete = false;
        this.isReviewUploadComplete = false;
        this.currentCursor = 0;
    }

    public void updateIsStoreUploadComplete(boolean isStoreUploadComplete) {
        this.isStoreUploadComplete = isStoreUploadComplete;
    }

    public void updateIsBoardUploadComplete(boolean isProductUploadComplete) {
        this.isBoardUploadComplete = isProductUploadComplete;
    }

    public void updateIsReviewUploadComplete(int reviewDataSize) {
        this.isReviewUploadComplete = reviewDataSize < LIMIT_REVIEW_DATA;
    }

    public Boolean continueSchedule(int reviewDataSize) {
        return reviewDataSize >= LIMIT_REVIEW_DATA;
    }

    public void incrementCursor() {
        this.currentCursor ++;
    }

    public Integer getOffSet() {
        return LIMIT_REVIEW_DATA * currentCursor;
    }

    public Integer getLimit() {
        return LIMIT_REVIEW_DATA;
    }
}

