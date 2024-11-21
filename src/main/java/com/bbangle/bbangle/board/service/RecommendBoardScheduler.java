package com.bbangle.bbangle.board.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendBoardScheduler {

    private final RecommendBoardService recommendBoardService;
    private boolean isScheduleActive = true;

    @Scheduled(cron = "0 0/3 3 * * ?")
    public void scheduleRecommendBoardUpdate() {
        if (!isScheduleActive) {
            log.info("스케줄링이 중지되었습니다.");
            return;
        }

        log.info("추천 데이터 업데이트 시작");
        isScheduleActive = recommendBoardService.saveRecommendBoardEntity();
    }
}
