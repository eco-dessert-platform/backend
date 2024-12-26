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
public class RecommendBoardConfig {

    private static final int MAX_PRODUCT_COUNT_NULL = -1;
    private static final int NON_MAX_PRODUCT_COUNT = 0;
    private static final int START_CURSOR_POINT = 0;

    @Id
    private RedisKeyEnum id;

    private Integer maxProductCount;

    private Integer nextCursor;

    public static RecommendBoardConfig create(
        Integer maxProductCount,
        Integer nextCursor
    ) {
        return RecommendBoardConfig.builder()
            .id(RedisKeyEnum.RECOMMENDATION_CONFIG)
            .maxProductCount(maxProductCount)
            .nextCursor(nextCursor)
            .build();
    }

    public void updateNextCursor(int nextCursor) {
        this.nextCursor = nextCursor;
    }

    public static RecommendBoardConfig empty() {
        return RecommendBoardConfig.builder()
            .id(RedisKeyEnum.RECOMMENDATION_CONFIG)
            .maxProductCount(MAX_PRODUCT_COUNT_NULL)
            .nextCursor(START_CURSOR_POINT)
            .build();
    }

    public static RecommendBoardConfig schedulingOff() {
        return RecommendBoardConfig.builder()
            .id(RedisKeyEnum.RECOMMENDATION_CONFIG)
            .maxProductCount(NON_MAX_PRODUCT_COUNT)
            .nextCursor(START_CURSOR_POINT)
            .build();
    }
}

