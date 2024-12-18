package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.domain.RecommendBoardConfig;
import com.bbangle.bbangle.board.repository.temp.RecommendationSimilarBoardMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendBoardScheduler {

    private final RecommendationSimilarBoardMemoryRepository memoryRepository;
    private final RecommendBoardService recommendBoardService;

    @Scheduled(cron = "0 0/3 3 * * ?")
    public void scheduleRecommendBoardUpdate() {
        try {
            log.info("추천 데이터 업데이트 시작");
            recommendBoardService.saveRecommendBoardEntity();
        } catch (Exception e) {
            log.error("스케줄링 중 에러 발생: {}", e.getMessage(), e);
            memoryRepository.save(RecommendBoardConfig.schedulingOff());
            throw e;
        }
    }

    public void executeUploadTask() {

    }
}
