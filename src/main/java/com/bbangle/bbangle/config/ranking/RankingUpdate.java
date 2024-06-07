package com.bbangle.bbangle.config.ranking;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import com.bbangle.bbangle.ranking.domain.BoardStatistic;
import com.bbangle.bbangle.ranking.repository.RankingRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RankingUpdate {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH");
    private final RankingRepository rankingRepository;
    @Autowired
    @Qualifier("boardLikeInfoRedisTemplate")
    private final RedisTemplate<String, Object> boardLikeInfoRedisTemplate;

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // 매시 정각마다 실행
    public void cleanupLikes() {
        LocalDateTime updateDate = LocalDateTime.now().minusDays(1);

        Set<String> keys = boardLikeInfoRedisTemplate.keys("*");
        if (keys != null) {
            for (String key : keys) {
                LocalDateTime keyTime;
                try {
                    keyTime = LocalDateTime.parse(key, formatter);
                } catch (Exception e) {
                    continue;
                }
                if (keyTime.isBefore(updateDate) || keyTime.isEqual(updateDate)) {
                    List<Object> range = boardLikeInfoRedisTemplate.opsForList()
                        .range(keyTime.format(formatter), 0, -1);
                    if (range == null) {
                        continue;
                    }
                    for (Object ele : range) {
                        BoardLikeInfo info = (BoardLikeInfo) ele;
                        BoardStatistic boardStatistic = rankingRepository.findByBoardId(info.boardId()).orElseThrow(() -> new BbangleException(BbangleErrorCode.RANKING_NOT_FOUND));
                        if (info.scoreType() == ScoreType.WISH) {
                            boardStatistic.updateBasicScore(-info.score());
                            continue;
                        }
                        boardStatistic.updateBasicScore(-info.score());
                    }
                    boardLikeInfoRedisTemplate.delete(key);
                }
            }
        }

    }

}
