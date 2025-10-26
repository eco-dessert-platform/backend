package com.bbangle.bbangle.boardstatistic.customer.ranking;

import com.bbangle.bbangle.boardstatistic.customer.service.BoardStatisticService;
import com.bbangle.bbangle.boardstatistic.customer.update.StatisticUpdate;
import com.bbangle.bbangle.boardstatistic.customer.update.UpdateType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateBoardStatistic {

    private static final String STATISTIC_UPDATE_LIST = "STATISTIC_UPDATE_LIST";
    @Qualifier("updateRedisTemplate")
    private final RedisTemplate<String, Object> updateRedisTemplate;
    private final BoardStatisticService boardStatisticService;

    @Scheduled(cron = "0 0 * * * *")
    @CacheEvict(value = "recommendContents", key = "'defaultRecommendCache'", cacheManager = "contentCacheManager")
    public void updateStatistic() {
        log.info("start update ranking");

        if (updateRedisTemplate.opsForList()
            .size(STATISTIC_UPDATE_LIST) == 0) {
            return;
        }

        List<Long> boardWishUpdateId = new ArrayList<>();
        List<Long> boardReviewUpdateId = new ArrayList<>();
        Map<Long, Integer> boardViewCountUpdate = new HashMap<>();
        List<Long> allUpdateBoard = new ArrayList<>();

        while (updateRedisTemplate.opsForList()
            .size(STATISTIC_UPDATE_LIST) > 0) {
            Object updateInfo = updateRedisTemplate.opsForList()
                .leftPop(STATISTIC_UPDATE_LIST);
            if (updateInfo instanceof StatisticUpdate update) {
                allUpdateBoard.add(update.boardId());
                log.info("statistic{}", updateInfo);

                if (update.updateType() == UpdateType.VIEW_COUNT) {
                    boardViewCountUpdate.put(update.boardId(),
                        boardViewCountUpdate.getOrDefault(update.boardId(), 0) + 1);
                }

                if (update.updateType() == UpdateType.REVIEW) {
                    boardReviewUpdateId.add(update.boardId());
                }

                if (update.updateType() == UpdateType.WISH_COUNT) {
                    boardWishUpdateId.add(update.boardId());
                }
            }
        }
        boardStatisticService.updateInBatch(boardWishUpdateId, boardReviewUpdateId,
            boardViewCountUpdate, allUpdateBoard);
        log.info("end update ranking");


    }

}
