package com.bbangle.bbangle.boardstatistic.ranking;

import com.bbangle.bbangle.boardstatistic.service.BoardStatisticService;
import com.bbangle.bbangle.boardstatistic.update.StatisticUpdate;
import com.bbangle.bbangle.boardstatistic.update.UpdateType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateBoardStatistic {

    private static final String STATISTIC_UPDATE_LIST = "STATISTIC_UPDATE_LIST";

    private final RedisTemplate<String, Object> redisTemplate;
    private final BoardStatisticService boardStatisticService;

    @Scheduled(cron = "0 0 * * * *")
    public void updateStatistic() {
        if (redisTemplate.opsForList()
            .size(STATISTIC_UPDATE_LIST) == 0) {
            return;
        }
        List<Long> boardWishUpdateId = new ArrayList<>();
        List<Long> boardReviewUpdateId = new ArrayList<>();
        Map<Long, Integer> boardViewCountUpdate = new HashMap<>();
        List<Long> allUpdateBoard = new ArrayList<>();

        for (int i = 0; i < redisTemplate.opsForList()
            .size(STATISTIC_UPDATE_LIST); i++) {
            Object updateInfo = redisTemplate.opsForList()
                .leftPop(STATISTIC_UPDATE_LIST);
            if (updateInfo instanceof StatisticUpdate update) {
                allUpdateBoard.add(update.boardId());

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

            boardStatisticService.updateInBatch(boardWishUpdateId, boardReviewUpdateId, boardViewCountUpdate, allUpdateBoard);
        }


    }

}
