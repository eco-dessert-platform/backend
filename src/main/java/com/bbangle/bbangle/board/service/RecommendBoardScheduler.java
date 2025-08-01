package com.bbangle.bbangle.board.service;

import com.bbangle.bbangle.board.domain.RecommendBoardConfig;
import com.bbangle.bbangle.board.repository.RecommendationSimilarBoardMemoryRepository;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendBoardScheduler {

    private final RecommendationSimilarBoardMemoryRepository memoryRepository;
    private final RecommendAiBoardService recommendAiBoardService;

    @Scheduled(cron = "0 0/3 3 * * ?")
    public void scheduleRecommendBoardUpdate() {
        try {
            log.info("추천 데이터 업데이트 시작");
            recommendAiBoardService.saveRecommendBoardEntity();
        } catch (Exception e) {
            log.error("스케줄링 중 에러 발생: {}", e.getMessage(), e);
            memoryRepository.save(RecommendBoardConfig.schedulingOff());
            throw e;
        }
    }

    public void startUploadTask() {

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                log.info("AI 학습 데이터셋 업로드 시작");
                boolean isScheduleContinue = recommendAiBoardService.uploadAiLearningCsv();
                if (!isScheduleContinue) {
                    log.info("AI 학습 데이터셋 업로드 종료");
                    scheduler.shutdown();
                }
            } catch (Exception e) {
                log.info("AI 학습 데이터셋 업로드 에러");
                scheduler.shutdown();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
}
